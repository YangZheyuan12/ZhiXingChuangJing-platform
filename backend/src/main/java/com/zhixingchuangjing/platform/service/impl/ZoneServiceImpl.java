package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.request.ZoneRequests;
import com.zhixingchuangjing.platform.model.response.ZoneResponses;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.repository.ZoneCommandRepository;
import com.zhixingchuangjing.platform.repository.ZoneQueryRepository;
import com.zhixingchuangjing.platform.service.ZoneService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZoneServiceImpl implements ZoneService {

    private final ZoneQueryRepository zoneQuery;
    private final ZoneCommandRepository zoneCommand;
    private final ExhibitionQueryRepository exhibitionQuery;
    private final ObjectMapper objectMapper;

    public ZoneServiceImpl(ZoneQueryRepository zoneQuery,
                           ZoneCommandRepository zoneCommand,
                           ExhibitionQueryRepository exhibitionQuery,
                           ObjectMapper objectMapper) {
        this.zoneQuery = zoneQuery;
        this.zoneCommand = zoneCommand;
        this.exhibitionQuery = exhibitionQuery;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ZoneResponses.ZoneSummaryResponse> listZones(Long exhibitionId, Long userId, String role) {
        assertCanAccess(exhibitionId, userId, role);
        return zoneQuery.listByExhibition(exhibitionId);
    }

    @Override
    public ZoneResponses.ZoneResponse getZoneDetail(Long zoneId, Long userId, String role) {
        ZoneResponses.ZoneResponse zone = zoneQuery.findById(zoneId);
        if (zone == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40401, "展区不存在");
        }
        assertCanAccess(zone.exhibitionId(), userId, role);
        return zone;
    }

    @Override
    public Long createZone(Long exhibitionId, Long userId, String role, ZoneRequests.CreateZoneRequest request) {
        assertCanEdit(exhibitionId, userId, role);
        return zoneCommand.createZone(exhibitionId, request.zoneCode(), request.zoneType(),
                request.title(), request.subtitle(), request.description(),
                request.sortOrder(), request.backgroundUrl(), request.transitionIn());
    }

    @Override
    public void updateZone(Long zoneId, Long userId, String role, ZoneRequests.UpdateZoneRequest request) {
        Long exhibitionId = requireZoneExhibitionId(zoneId);
        assertCanEdit(exhibitionId, userId, role);
        zoneCommand.updateZone(zoneId, request.title(), request.subtitle(), request.description(),
                request.backgroundUrl(), toJson(request.backgroundStyle()),
                toJson(request.layoutConfig()), request.narrationText(),
                request.transitionIn(), request.sortOrder());
    }

    @Override
    public void updateCanvasData(Long zoneId, Long userId, String role, ZoneRequests.UpdateCanvasDataRequest request) {
        Long exhibitionId = requireZoneExhibitionId(zoneId);
        assertCanEdit(exhibitionId, userId, role);
        zoneCommand.updateCanvasData(zoneId, toJson(request.canvasData()));
    }

    @Override
    public void assignZone(Long zoneId, Long userId, String role, ZoneRequests.AssignZoneRequest request) {
        Long exhibitionId = requireZoneExhibitionId(zoneId);
        assertCanEdit(exhibitionId, userId, role);
        zoneCommand.assignZone(zoneId, request.assignedUserId());
    }

    @Override
    public void lockZone(Long zoneId, Long userId, String role, ZoneRequests.LockZoneRequest request) {
        Long exhibitionId = requireZoneExhibitionId(zoneId);
        assertCanEdit(exhibitionId, userId, role);
        if (request.lock()) {
            int updated = zoneCommand.lockZone(zoneId, userId);
            if (updated == 0) {
                throw new BusinessException(HttpStatus.CONFLICT, 40901, "该展区已被其他成员锁定");
            }
        } else {
            zoneCommand.unlockZone(zoneId, userId);
        }
    }

    @Override
    public void deleteZone(Long zoneId, Long userId, String role) {
        Long exhibitionId = requireZoneExhibitionId(zoneId);
        assertCanEdit(exhibitionId, userId, role);
        zoneCommand.deleteZone(zoneId);
    }

    private Long requireZoneExhibitionId(Long zoneId) {
        Long exhibitionId = zoneQuery.findExhibitionIdByZoneId(zoneId);
        if (exhibitionId == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40401, "展区不存在");
        }
        return exhibitionId;
    }

    private void assertCanAccess(Long exhibitionId, Long userId, String role) {
        if (!exhibitionQuery.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40301, "无权访问该展厅");
        }
    }

    private void assertCanEdit(Long exhibitionId, Long userId, String role) {
        if ("admin".equals(role)) return;
        if (!exhibitionQuery.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40302, "无权编辑该展厅");
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException(40010, "JSON序列化失败");
        }
    }
}
