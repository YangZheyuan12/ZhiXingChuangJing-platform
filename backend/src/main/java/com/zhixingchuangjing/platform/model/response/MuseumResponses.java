package com.zhixingchuangjing.platform.model.response;

import java.util.Map;

public final class MuseumResponses {

    private MuseumResponses() {
    }

    public record MuseumResourceResponse(
            Long id,
            String providerCode,
            String title,
            String category,
            String museumName,
            String coverUrl,
            String detailUrl,
            String description,
            Map<String, Object> metadata
    ) {
    }
}
