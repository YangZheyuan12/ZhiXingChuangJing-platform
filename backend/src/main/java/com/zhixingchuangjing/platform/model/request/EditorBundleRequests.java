package com.zhixingchuangjing.platform.model.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public final class EditorBundleRequests {
    private EditorBundleRequests() {}

    public record SaveBundleRequest(
        @NotNull(message = "revision不能为空") Integer revision,
        List<ZoneRequests.UpdateZoneRequest> zones,
        List<ExhibitRequests.UpdateExhibitRequest> exhibits,
        Map<String, Object> canvasDataMap
    ) {}

    public record DraftBundleRequest(
        @NotNull(message = "展区编码不能为空") String zoneCode,
        Object canvasData,
        List<ExhibitRequests.UpdateExhibitRequest> exhibits,
        ZoneRequests.UpdateZoneRequest zoneMetadata
    ) {}
}
