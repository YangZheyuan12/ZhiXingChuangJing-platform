package com.zhixingchuangjing.platform.model.response;

import java.time.LocalDateTime;
import java.util.List;

public final class ChatResponses {

    private ChatResponses() {
    }

    public record ChatMessageResponse(
            String id,
            String content,
            String role,
            Long timestamp
    ) {
    }

    public record ChatResponseData(
            ChatMessageResponse message,
            String conversationId
    ) {
    }

    public record ConversationInfo(
            String id,
            String title,
            Long createdAt,
            Integer messageCount
    ) {
    }

    public record CreateConversationResponse(
            String conversationId
    ) {
    }

    public record GetHistoryResponse(
            List<ChatMessageResponse> messages
    ) {
    }
}
