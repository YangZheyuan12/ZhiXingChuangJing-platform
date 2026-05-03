package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.HotspotRequests;
import com.zhixingchuangjing.platform.model.response.EditorBundleResponses;

import java.util.List;

public interface HotspotService {

    List<EditorBundleResponses.HotspotResponse> listByZone(Long exhibitionId, Long zoneId,
                                                           Long userId, String role);

    EditorBundleResponses.HotspotResponse createHotspot(Long exhibitionId, Long zoneId,
                                                        Long userId, String role,
                                                        HotspotRequests.CreateHotspotRequest request);

    EditorBundleResponses.HotspotResponse updateHotspot(Long exhibitionId, Long hotspotId,
                                                        Long userId, String role,
                                                        HotspotRequests.UpdateHotspotRequest request);

    void deleteHotspot(Long exhibitionId, Long hotspotId, Long userId, String role);
}
