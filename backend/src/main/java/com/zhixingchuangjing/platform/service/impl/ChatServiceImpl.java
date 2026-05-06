package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.entity.ChatConversationEntity;
import com.zhixingchuangjing.platform.entity.ChatMessageEntity;
import com.zhixingchuangjing.platform.model.request.ChatRequests;
import com.zhixingchuangjing.platform.model.response.ChatResponses;
import com.zhixingchuangjing.platform.repository.ChatConversationRepository;
import com.zhixingchuangjing.platform.repository.ChatMessageRepository;
import com.zhixingchuangjing.platform.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api.key:sk-0421479e732446ffb4aa6b4f4d6cb2f2}")
    private String deepseekApiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/chat/completions}")
    private String deepseekApiUrl;

    private static final String DEEPSEEK_MODEL = "deepseek-chat";

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
    public ChatResponses.CreateConversationResponse createConversation(Long userId) {
        String conversationId = UUID.randomUUID().toString();
        ChatConversationEntity conversation = new ChatConversationEntity(
                conversationId,
                userId,
                "新对话"
        );
        chatConversationRepository.save(conversation);
        return new ChatResponses.CreateConversationResponse(conversationId);
    }

    @Override
    public ChatResponses.ChatResponseData sendChatMessage(Long userId, ChatRequests.SendChatMessageRequest request) {
        String conversationId = request.conversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
            ChatConversationEntity conversation = new ChatConversationEntity(
                    conversationId,
                    userId,
                    "新对话"
            );
            chatConversationRepository.save(conversation);
        }

        // 保存用户消息
        ChatMessageEntity userMessage = new ChatMessageEntity(
                conversationId,
                userId,
                request.content(),
                "user"
        );
        chatMessageRepository.save(userMessage);

        // 调用Deepseek API
        String aiResponse = callDeepseekAPI(request.content(), conversationId, userId);

        // 保存AI回复
        ChatMessageEntity assistantMessage = new ChatMessageEntity(
                conversationId,
                null,
                aiResponse,
                "assistant"
        );
        chatMessageRepository.save(assistantMessage);

        // 更新对话信息
        ChatConversationEntity conversation = chatConversationRepository.findById(conversationId)
                .orElse(new ChatConversationEntity(conversationId, userId, "新对话"));
        conversation.setMessageCount(conversation.getMessageCount() + 2);
        conversation.setUpdatedAt(LocalDateTime.now());
        if (conversation.getTitle().equals("新对话") && request.content().length() > 0) {
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
    public void deleteConversation(Long userId, String conversationId) {
        // 删除对话的所有消息
        List<ChatMessageEntity> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        chatMessageRepository.deleteAll(messages);
        
        // 删除对话
        ChatConversationEntity conversation = chatConversationRepository.findById(conversationId).orElse(null);
        if (conversation != null && conversation.getUserId().equals(userId)) {
            chatConversationRepository.delete(conversation);
        }
    }

    /**
     * 调用Deepseek API获取回复
     */
    private String callDeepseekAPI(String userMessage, String conversationId, Long userId) {
        try {
            // 获取对话历史
            List<ChatMessageEntity> history = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            for (ChatMessageEntity msg : history) {
                messages.add(Map.of(
                        "role", msg.getRole(),
                        "content", msg.getContent()
                ));
            }

            // 构建请求体
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", DEEPSEEK_MODEL);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey);

            // 发送请求
            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers
            );

            String response = restTemplate.postForObject(deepseekApiUrl, entity, String.class);

            // 解析响应
            JsonNode responseNode = objectMapper.readTree(response);
            String content = responseNode.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("抱歉，我暂时无法回复。");

            return content;

        } catch (Exception e) {
            return "抱歉，发生错误：" + e.getMessage();
        }
    }
}
