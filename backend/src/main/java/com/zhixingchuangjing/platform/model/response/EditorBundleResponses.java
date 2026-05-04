package com.zhixingchuangjing.platform.model.response;

import java.util.List;

public final class EditorBundleResponses {
    private EditorBundleResponses() {}

    public record EditorBundleResponse(
        ExhibitionResponses.ExhibitionDetailResponse exhibition,
        List<ZoneResponses.ZoneResponse> zones,
        List<ExhibitResponses.ExhibitResponse> exhibits,
        List<HotspotResponse> hotspots,
        ExhibitionResponses.DigitalHumanResponse digitalHuman,
        List<ExhibitionResponses.DigitalHumanResponse> digitalHumans,
        List<ZoneResponses.ZoneDigitalHumanPlacementResponse> zoneDigitalHumans,
        Object template,
        Integer revision
    ) {}

    public record HotspotResponse(
        Long id, Long zoneId, Long targetZoneId,
        String hotspotType, String label, String icon,
        Double xPercent, Double yPercent, Double wPercent, Double hPercent,
        Object styleJson, Object actionConfig, Integer sortOrder
    ) {}

    public record SaveBundleResultResponse(
        Integer revision
    ) {}
}
