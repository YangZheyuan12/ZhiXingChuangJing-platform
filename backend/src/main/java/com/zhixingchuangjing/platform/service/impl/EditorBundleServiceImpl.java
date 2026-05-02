package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.request.EditorBundleRequests;
import com.zhixingchuangjing.platform.model.request.ExhibitRequests;
import com.zhixingchuangjing.platform.model.request.ZoneRequests;
import com.zhixingchuangjing.platform.model.response.EditorBundleResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitResponses;
import com.zhixingchuangjing.platform.model.response.ZoneResponses;
import com.zhixingchuangjing.platform.repository.*;
import com.zhixingchuangjing.platform.service.EditorBundleService;
import com.zhixingchuangjing.platform.service.ExhibitService;
import com.zhixingchuangjing.platform.service.ExhibitionService;
import com.zhixingchuangjing.platform.service.ZoneService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EditorBundleServiceImpl implements EditorBundleService {

    private final ExhibitionQueryRepository exhibitionQuery;
    private final ExhibitionCommandRepository exhibitionCommand;
    private final ZoneQueryRepository zoneQuery;
    private final ZoneCommandRepository zoneCommand;
    private final ExhibitQueryRepository exhibitQuery;
    private final HotspotQueryRepository hotspotQuery;
    private final ExhibitionService exhibitionService;
    private final ZoneService zoneService;
    private final ExhibitService exhibitService;
    private final ObjectMapper objectMapper;

    public EditorBundleServiceImpl(ExhibitionQueryRepository exhibitionQuery,
                                   ExhibitionCommandRepository exhibitionCommand,
                                   ZoneQueryRepository zoneQuery,
                                   ZoneCommandRepository zoneCommand,
                                   ExhibitQueryRepository exhibitQuery,
                                   HotspotQueryRepository hotspotQuery,
                                   ExhibitionService exhibitionService,
                                   ZoneService zoneService,
                                   ExhibitService exhibitService,
                                   ObjectMapper objectMapper) {
        this.exhibitionQuery = exhibitionQuery;
        this.exhibitionCommand = exhibitionCommand;
        this.zoneQuery = zoneQuery;
        this.zoneCommand = zoneCommand;
        this.exhibitQuery = exhibitQuery;
        this.hotspotQuery = hotspotQuery;
        this.exhibitionService = exhibitionService;
        this.zoneService = zoneService;
        this.exhibitService = exhibitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public EditorBundleResponses.EditorBundleResponse getBundle(Long exhibitionId, Long userId, String role) {
        assertCanEdit(exhibitionId, userId, role);
        var exhibition = exhibitionService.getExhibitionDetail(exhibitionId, userId, role);
        var zones = zoneQuery.listByExhibition(exhibitionId).stream()
            .map(z -> zoneQuery.findById(z.id()))
            .toList();
        List<ExhibitResponses.ExhibitResponse> allExhibits = new ArrayList<>();
        for (var zone : zones) {
            var summaries = exhibitQuery.listByZone(zone.id());
            for (var s : summaries) {
                var detail = exhibitQuery.findById(s.id());
                if (detail != null) {
                    var narrations = exhibitQuery.listNarrations(s.id());
                    var interactions = exhibitQuery.listInteractions(s.id());
                    allExhibits.add(new ExhibitResponses.ExhibitResponse(
                        detail.id(), detail.exhibitionId(), detail.zoneId(),
                        detail.slotCode(), detail.placementMode(), detail.placementJson(),
                        detail.title(), detail.subtitle(), detail.exhibitType(),
                        detail.coverUrl(), detail.mediaUrl(),
                        detail.sourceType(), detail.museumResourceId(), detail.mediaAssetId(),
                        detail.description(), detail.sourceInfo(), detail.knowledgePoints(),
                        detail.sortOrder(), detail.status(),
                        narrations, interactions,
                        detail.createdAt(), detail.updatedAt()
                    ));
                }
            }
        }
        var hotspots = hotspotQuery.listByExhibition(exhibitionId);
        Integer revision = exhibitionQuery.getBundleRevision(exhibitionId);
        return new EditorBundleResponses.EditorBundleResponse(
            exhibition, zones, allExhibits, hotspots, null, null, revision
        );
    }

    @Override
    @Transactional
    public EditorBundleResponses.SaveBundleResultResponse saveBundle(
            Long exhibitionId, Long userId, String role,
            EditorBundleRequests.SaveBundleRequest request) {
        assertCanEdit(exhibitionId, userId, role);
        int updated = exhibitionCommand.incrementBundleRevisionIfMatch(exhibitionId, request.revision());
        if (updated == 0) {
            throw new BusinessException(HttpStatus.CONFLICT, 40901,
                "展厅内容已被其他成员更新，请刷新后重试");
        }
        if (request.zones() != null) {
            for (var zoneReq : request.zones()) {
                zoneService.updateZone(findZoneIdByCode(exhibitionId, zoneReq), userId, role, zoneReq);
            }
        }
        if (request.exhibits() != null) {
            for (var exhibitReq : request.exhibits()) {
                exhibitService.updateExhibit(findExhibitId(exhibitReq), userId, role, exhibitReq);
            }
        }
        if (request.canvasDataMap() != null) {
            for (Map.Entry<String, Object> entry : request.canvasDataMap().entrySet()) {
                String zoneCode = entry.getKey();
                var zoneId = findZoneIdByZoneCode(exhibitionId, zoneCode);
                zoneCommand.updateCanvasData(zoneId, toJson(entry.getValue()));
            }
        }
        Integer newRevision = exhibitionCommand.getCurrentBundleRevision(exhibitionId);
        return new EditorBundleResponses.SaveBundleResultResponse(newRevision);
    }

    @Override
    @Transactional
    public void saveDraftBundle(Long exhibitionId, Long userId, String role,
                                EditorBundleRequests.DraftBundleRequest request) {
        assertCanEdit(exhibitionId, userId, role);
        var zones = zoneQuery.listByExhibition(exhibitionId);
        var targetZone = zones.stream()
            .filter(z -> z.zoneCode().equals(request.zoneCode()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40402, "展区不存在"));
        if (request.canvasData() != null) {
            zoneCommand.updateCanvasData(targetZone.id(), toJson(request.canvasData()));
        }
        if (request.zoneMetadata() != null) {
            zoneService.updateZone(targetZone.id(), userId, role, request.zoneMetadata());
        }
        if (request.exhibits() != null) {
            for (var exhibitReq : request.exhibits()) {
                exhibitService.updateExhibit(findExhibitId(exhibitReq), userId, role, exhibitReq);
            }
        }
    }

    private Long findZoneIdByZoneCode(Long exhibitionId, String zoneCode) {
        var zones = zoneQuery.listByExhibition(exhibitionId);
        return zones.stream()
            .filter(z -> z.zoneCode().equals(zoneCode))
            .findFirst()
            .map(ZoneResponses.ZoneSummaryResponse::id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40402, "展区不存在: " + zoneCode));
    }

    private Long findZoneIdByCode(Long exhibitionId, ZoneRequests.UpdateZoneRequest req) {
        // bundle save request should include zoneId per zone in actual frontend payload
        return exhibitionId;
    }

    private Long findExhibitId(ExhibitRequests.UpdateExhibitRequest req) {
        // bundle save request should include exhibitId per exhibit in actual frontend payload
        return 0L;
    }

    private void assertCanEdit(Long exhibitionId, Long userId, String role) {
        if ("admin".equals(role)) return;
        if (!exhibitionQuery.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40302, "无权编辑该展厅");
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { throw new BusinessException(40010, "JSON序列化失败"); }
    }
}
