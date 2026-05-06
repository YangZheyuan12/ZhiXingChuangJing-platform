package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.entity.ChatConversationEntity;
import com.zhixingchuangjing.platform.entity.ChatMessageEntity;
import com.zhixingchuangjing.platform.model.request.ChatRequests;
import com.zhixingchuangjing.platform.model.response.ChatResponses;
import com.zhixingchuangjing.platform.repository.ChatConversationRepository;
import com.zhixingchuangjing.platform.repository.ChatMessageRepository;
import com.zhixingchuangjing.platform.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);
    private static final String DEFAULT_CONVERSATION_TITLE = "新对话";
    private static final String DEEPSEEK_MODEL = "deepseek-chat";

    private final ChatMessageRepository chatMessageRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api.key:}")
    private String deepseekApiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/chat/completions}")
    private String deepseekApiUrl;

    public ChatServiceImpl(ChatMessageRepository chatMessageRepository,
                         ChatConversationRepository chatConversationRepository,
                         RestTemplate restTemplate,
                         ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatConversationRepository = chatConversationRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ChatResponses.CreateConversationResponse createConversation(Long userId) {
        String conversationId = UUID.randomUUID().toString();
        ChatConversationEntity conversation = new ChatConversationEntity(
                conversationId,
                userId,
                DEFAULT_CONVERSATION_TITLE
        );
        chatConversationRepository.save(conversation);
        return new ChatResponses.CreateConversationResponse(conversationId);
    }

    @Override
    @Transactional
    public ChatResponses.ChatResponseData sendChatMessage(Long userId, ChatRequests.SendChatMessageRequest request) {
        ChatConversationEntity conversation = resolveConversation(userId, request.conversationId());
        String conversationId = conversation.getId();

        ChatMessageEntity userMessage = new ChatMessageEntity(
                conversationId,
                userId,
                request.content(),
                "user"
        );
        chatMessageRepository.save(userMessage);

        String aiResponse = callDeepseekAPI(request.content(), conversationId, userId);

        ChatMessageEntity assistantMessage = new ChatMessageEntity(
                conversationId,
                null,
                aiResponse,
                "assistant"
        );
        chatMessageRepository.save(assistantMessage);

        conversation.setMessageCount(conversation.getMessageCount() + 2);
        conversation.setUpdatedAt(LocalDateTime.now());
        if (DEFAULT_CONVERSATION_TITLE.equals(conversation.getTitle()) && !request.content().isBlank()) {
            conversation.setTitle(request.content().substring(0, Math.min(50, request.content().length())));
        }
        chatConversationRepository.save(conversation);

        ChatResponses.ChatMessageResponse messageResponse = new ChatResponses.ChatMessageResponse(
                String.valueOf(assistantMessage.getId()),
                assistantMessage.getContent(),
                "assistant",
                assistantMessage.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        );

        return new ChatResponses.ChatResponseData(messageResponse, conversationId);
    }

    @Override
    public List<ChatResponses.ChatMessageResponse> getChatHistory(Long userId, String conversationId) {
        requireConversationOwner(userId, conversationId);
        List<ChatMessageEntity> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream()
                .map(msg -> new ChatResponses.ChatMessageResponse(
                        String.valueOf(msg.getId()),
                        msg.getContent(),
                        msg.getRole(),
                        msg.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatResponses.ConversationInfo> getUserConversations(Long userId) {
        List<ChatConversationEntity> conversations = chatConversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return conversations.stream()
                .map(conv -> new ChatResponses.ConversationInfo(
                        conv.getId(),
                        conv.getTitle(),
                        conv.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        conv.getMessageCount()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteConversation(Long userId, String conversationId) {
        requireConversationOwner(userId, conversationId);
        List<ChatMessageEntity> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        chatMessageRepository.deleteAll(messages);
        ChatConversationEntity conversation = getConversationOrThrow(userId, conversationId);
        chatConversationRepository.delete(conversation);
    }

    private String callDeepseekAPI(String userMessage, String conversationId, Long userId) {
        if (deepseekApiKey == null || deepseekApiKey.isBlank()) {
            return "AI 服务暂未配置，请联系管理员。";
        }
        try {
            List<ChatMessageEntity> history = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

            List<Map<String, String>> messages = new ArrayList<>();
            for (ChatMessageEntity msg : history) {
                messages.add(Map.of(
                        "role", msg.getRole(),
                        "content", msg.getContent()
                ));
            }

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", DEEPSEEK_MODEL);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey);

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            String response = restTemplate.postForObject(deepseekApiUrl, entity, String.class);

            JsonNode responseNode = objectMapper.readTree(response);
            return responseNode.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("抱歉，我暂时无法回复。");
        } catch (Exception e) {
            log.warn("Deepseek chat request failed userId={} conversationId={} messageLength={}",
                    userId, conversationId, userMessage == null ? 0 : userMessage.length(), e);
            return "抱歉，AI 服务暂时不可用，请稍后再试。";
        }
    }

    private ChatConversationEntity resolveConversation(Long userId, String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            String newConversationId = UUID.randomUUID().toString();
            ChatConversationEntity conversation = new ChatConversationEntity(
                    newConversationId,
                    userId,
                    DEFAULT_CONVERSATION_TITLE
            );
            return chatConversationRepository.save(conversation);
        }
        return getConversationOrThrow(userId, conversationId);
    }

    private void requireConversationOwner(Long userId, String conversationId) {
        getConversationOrThrow(userId, conversationId);
    }

    private ChatConversationEntity getConversationOrThrow(Long userId, String conversationId) {
        return chatConversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40421, "对话不存在"));
    }
}
