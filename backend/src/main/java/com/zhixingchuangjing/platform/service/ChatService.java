package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.ChatRequests;
import com.zhixingchuangjing.platform.model.response.ChatResponses;

import java.util.List;

public interface ChatService {
    
    /**
     * 创建新的对话
     */
    ChatResponses.CreateConversationResponse createConversation(Long userId);
    
    /**
     * 发送聊天消息并获取AI回复
     */
    ChatResponses.ChatResponseData sendChatMessage(Long userId, ChatRequests.SendChatMessageRequest request);
    
    /**
     * 获取对话历史
     */
    List<ChatResponses.ChatMessageResponse> getChatHistory(Long userId, String conversationId);
    
    /**
     * 获取用户的所有对话
     */
    List<ChatResponses.ConversationInfo> getUserConversations(Long userId);
    
    /**
     * 删除对话
     */
    void deleteConversation(Long userId, String conversationId);
}
