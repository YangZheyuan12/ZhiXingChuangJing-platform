package com.zhixingchuangjing.platform.model.response;

import java.util.List;

public final class AiResponses {

    private AiResponses() {
    }

    public record GenerateNarrationResponse(
            String narration,
            List<String> suggestions
    ) {
    }
}
