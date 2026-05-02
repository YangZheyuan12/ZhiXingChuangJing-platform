package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.ZoneRequests;
import com.zhixingchuangjing.platform.model.response.ZoneResponses;

import java.util.List;

public interface ZoneService {
    List<ZoneResponses.ZoneSummaryResponse> listZones(Long exhibitionId, Long userId, String role);
    ZoneResponses.ZoneResponse getZoneDetail(Long zoneId, Long userId, String role);
    Long createZone(Long exhibitionId, Long userId, String role, ZoneRequests.CreateZoneRequest request);
    void updateZone(Long zoneId, Long userId, String role, ZoneRequests.UpdateZoneRequest request);
    void updateCanvasData(Long zoneId, Long userId, String role, ZoneRequests.UpdateCanvasDataRequest request);
    void assignZone(Long zoneId, Long userId, String role, ZoneRequests.AssignZoneRequest request);
    void lockZone(Long zoneId, Long userId, String role, ZoneRequests.LockZoneRequest request);
    void deleteZone(Long zoneId, Long userId, String role);
}
