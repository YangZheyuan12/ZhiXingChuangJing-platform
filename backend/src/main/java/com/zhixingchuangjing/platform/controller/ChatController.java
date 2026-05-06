package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.ChatRequests;
import com.zhixingchuangjing.platform.model.response.ChatResponses;
import com.zhixingchuangjing.platform.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/chat")
public class ChatController extends BaseController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 创建新对话
     */
    @PostMapping("/conversation")
    public ApiResponse<ChatResponses.CreateConversationResponse> createConversation(
            @AuthenticationPrincipal SecurityUserDetails user) {
        return success(chatService.createConversation(user.getId()));
    }

    /**
     * 发送聊天消息
     */
    @PostMapping("/message")
    public ApiResponse<ChatResponses.ChatResponseData> sendChatMessage(
            @AuthenticationPrincipal SecurityUserDetails user,
            @Valid @RequestBody ChatRequests.SendChatMessageRequest request) {
        return success(chatService.sendChatMessage(user.getId(), request));
    }

    /**
     * 获取对话历史
     */
    @GetMapping("/history/{conversationId}")
    public ApiResponse<List<ChatResponses.ChatMessageResponse>> getChatHistory(
            @AuthenticationPrincipal SecurityUserDetails user,
            @PathVariable String conversationId) {
        return success(chatService.getChatHistory(user.getId(), conversationId));
    }

    /**
     * 获取用户的所有对话
     */
    @GetMapping("/conversations")
    public ApiResponse<List<ChatResponses.ConversationInfo>> getUserConversations(
            @AuthenticationPrincipal SecurityUserDetails user) {
        return success(chatService.getUserConversations(user.getId()));
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/conversation/{conversationId}")
    public ApiResponse<Void> deleteConversation(
            @AuthenticationPrincipal SecurityUserDetails user,
            @PathVariable String conversationId) {
        chatService.deleteConversation(user.getId(), conversationId);
        return success(null);
    }
}
