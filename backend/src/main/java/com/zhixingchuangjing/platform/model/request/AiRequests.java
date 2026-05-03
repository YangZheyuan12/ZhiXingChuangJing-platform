package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AiRequests {

    private AiRequests() {
    }

    public record GenerateNarrationRequest(
            @NotBlank(message = "展品名称不能为空")
            @Size(max = 120, message = "展品名称长度不能超过120个字符")
            String exhibitTitle,
            @Size(max = 2000, message = "展品描述长度不能超过2000个字符")
            String exhibitDescription,
            @Size(max = 20, message = "知识点数量不能超过20个")
            List<@Size(max = 200) String> knowledgePoints,
            @Size(max = 32, message = "目标年级长度不能超过32个字符")
            String targetGrade,
            @Size(max = 32, message = "风格标识长度不能超过32个字符")
            String style,
            Integer maxLength
    ) {
    }
}
