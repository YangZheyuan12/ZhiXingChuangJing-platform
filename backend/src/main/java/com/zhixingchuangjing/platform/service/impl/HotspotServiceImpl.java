package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.request.HotspotRequests;
import com.zhixingchuangjing.platform.model.response.EditorBundleResponses;
import com.zhixingchuangjing.platform.repository.ExhibitionCommandRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.repository.HotspotCommandRepository;
import com.zhixingchuangjing.platform.repository.HotspotQueryRepository;
import com.zhixingchuangjing.platform.service.HotspotService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HotspotServiceImpl implements HotspotService {

    private final HotspotCommandRepository hotspotCommand;
    private final HotspotQueryRepository hotspotQuery;
    private final ExhibitionQueryRepository exhibitionQuery;
    private final ExhibitionCommandRepository exhibitionCommand;
    private final ObjectMapper objectMapper;

    public HotspotServiceImpl(HotspotCommandRepository hotspotCommand,
                              HotspotQueryRepository hotspotQuery,
                              ExhibitionQueryRepository exhibitionQuery,
                              ExhibitionCommandRepository exhibitionCommand,
                              ObjectMapper objectMapper) {
        this.hotspotCommand = hotspotCommand;
        this.hotspotQuery = hotspotQuery;
        this.exhibitionQuery = exhibitionQuery;
        this.exhibitionCommand = exhibitionCommand;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<EditorBundleResponses.HotspotResponse> listByZone(Long exhibitionId, Long zoneId,
                                                                  Long userId, String role) {
        assertCanEdit(exhibitionId, userId, role);
        if (!hotspotQuery.zoneBelongsToExhibition(zoneId, exhibitionId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40402, "展区不存在或不属于该展厅");
        }
        return hotspotQuery.listByZone(zoneId);
    }

    @Override
    @Transactional
    public EditorBundleResponses.HotspotResponse createHotspot(Long exhibitionId, Long zoneId,
                                                               Long userId, String role,
                                                               HotspotRequests.CreateHotspotRequest request) {
        assertCanEdit(exhibitionId, userId, role);
        if (!hotspotQuery.zoneBelongsToExhibition(zoneId, exhibitionId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40402, "展区不存在或不属于该展厅");
        }
        Long id = hotspotCommand.createHotspot(
                zoneId,
                request.targetZoneId(),
                request.hotspotType(),
                trim(request.label()),
                trim(request.icon()),
                request.xPercent(),
                request.yPercent(),
                request.wPercent(),
                request.hPercent(),
                toJson(request.styleJson()),
                toJson(request.actionConfig()),
                request.sortOrder()
        );
        exhibitionCommand.incrementBundleRevision(exhibitionId);
        return hotspotQuery.findById(id);
    }

    @Override
    @Transactional
    public EditorBundleResponses.HotspotResponse updateHotspot(Long exhibitionId, Long hotspotId,
                                                               Long userId, String role,
                                                               HotspotRequests.UpdateHotspotRequest request) {
        assertCanEdit(exhibitionId, userId, role);
        Long existing = hotspotQuery.findExhibitionIdByHotspotId(hotspotId);
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40403, "热点不存在");
        }
        if (!existing.equals(exhibitionId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40303, "热点不属于该展厅");
        }
        int affected = hotspotCommand.updateHotspot(
                hotspotId,
                request.targetZoneId(),
                request.hotspotType(),
                trim(request.label()),
                trim(request.icon()),
                request.xPercent(),
                request.yPercent(),
                request.wPercent(),
                request.hPercent(),
                toJson(request.styleJson()),
                toJson(request.actionConfig()),
                request.sortOrder()
        );
        if (affected == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40403, "热点不存在或已被删除");
        }
        exhibitionCommand.incrementBundleRevision(exhibitionId);
        return hotspotQuery.findById(hotspotId);
    }

    @Override
    @Transactional
    public void deleteHotspot(Long exhibitionId, Long hotspotId, Long userId, String role) {
        assertCanEdit(exhibitionId, userId, role);
        Long existing = hotspotQuery.findExhibitionIdByHotspotId(hotspotId);
        if (existing == null) {
            return;
        }
        if (!existing.equals(exhibitionId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40303, "热点不属于该展厅");
        }
        hotspotCommand.deleteHotspot(hotspotId);
        exhibitionCommand.incrementBundleRevision(exhibitionId);
    }

    private void assertCanEdit(Long exhibitionId, Long userId, String role) {
        if ("admin".equals(role)) return;
        if (!exhibitionQuery.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40302, "无权编辑该展厅");
        }
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String str) {
            return str.isBlank() ? null : str;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException(40010, "JSON 序列化失败");
        }
    }
}
