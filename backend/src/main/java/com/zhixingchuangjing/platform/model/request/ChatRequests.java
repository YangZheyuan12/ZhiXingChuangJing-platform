package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;

public final class ChatRequests {

    private ChatRequests() {
    }

    public record SendChatMessageRequest(
            @NotBlank(message = "消息内容不能为空")
            String content,
            String conversationId
    ) {
    }
}
