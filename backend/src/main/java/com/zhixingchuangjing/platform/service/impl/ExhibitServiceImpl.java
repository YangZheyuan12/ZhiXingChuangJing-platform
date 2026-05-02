package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.request.ExhibitRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitResponses;
import com.zhixingchuangjing.platform.repository.ExhibitCommandRepository;
import com.zhixingchuangjing.platform.repository.ExhibitQueryRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.repository.ZoneQueryRepository;
import com.zhixingchuangjing.platform.service.ExhibitService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExhibitServiceImpl implements ExhibitService {

    private final ExhibitQueryRepository exhibitQuery;
    private final ExhibitCommandRepository exhibitCommand;
    private final ZoneQueryRepository zoneQuery;
    private final ExhibitionQueryRepository exhibitionQuery;
    private final ObjectMapper objectMapper;

    public ExhibitServiceImpl(ExhibitQueryRepository exhibitQuery,
                              ExhibitCommandRepository exhibitCommand,
                              ZoneQueryRepository zoneQuery,
                              ExhibitionQueryRepository exhibitionQuery,
                              ObjectMapper objectMapper) {
        this.exhibitQuery = exhibitQuery;
        this.exhibitCommand = exhibitCommand;
        this.zoneQuery = zoneQuery;
        this.exhibitionQuery = exhibitionQuery;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ExhibitResponses.ExhibitSummaryResponse> listExhibits(Long zoneId, Long userId, String role) {
        Long exhibitionId = zoneQuery.findExhibitionIdByZoneId(zoneId);
        if (exhibitionId == null) throw new BusinessException(HttpStatus.NOT_FOUND, 40401, "展区不存在");
        assertCanAccess(exhibitionId, userId, role);
        return exhibitQuery.listByZone(zoneId);
    }

    @Override
    public ExhibitResponses.ExhibitResponse getExhibitDetail(Long exhibitId, Long userId, String role) {
        ExhibitResponses.ExhibitResponse exhibit = exhibitQuery.findById(exhibitId);
        if (exhibit == null) throw new BusinessException(HttpStatus.NOT_FOUND, 40401, "展品不存在");
        assertCanAccess(exhibit.exhibitionId(), userId, role);
        var narrations = exhibitQuery.listNarrations(exhibitId);
        var interactions = exhibitQuery.listInteractions(exhibitId);
        return new ExhibitResponses.ExhibitResponse(
                exhibit.id(), exhibit.exhibitionId(), exhibit.zoneId(),
                exhibit.slotCode(), exhibit.placementMode(), exhibit.placementJson(),
                exhibit.title(), exhibit.subtitle(), exhibit.exhibitType(),
                exhibit.coverUrl(), exhibit.mediaUrl(),
                exhibit.sourceType(), exhibit.museumResourceId(), exhibit.mediaAssetId(),
                exhibit.description(), exhibit.sourceInfo(), exhibit.knowledgePoints(),
                exhibit.sortOrder(), exhibit.status(),
                narrations, interactions,
                exhibit.createdAt(), exhibit.updatedAt()
        );
    }

    @Override
    public Long createExhibit(Long exhibitionId, Long userId, String role, ExhibitRequests.CreateExhibitRequest request) {
        assertCanEdit(exhibitionId, userId, role);
        return exhibitCommand.createExhibit(exhibitionId, request.zoneId(),
                request.slotCode(), request.placementMode(), toJson(request.placementJson()),
                request.title(), request.subtitle(), request.exhibitType(),
                request.coverUrl(), request.mediaUrl(),
                request.sourceType(), request.museumResourceId(), request.mediaAssetId(),
                request.description(), toJson(request.sourceInfo()),
                toJson(request.knowledgePoints()), request.sortOrder());
    }

    @Override
    public void updateExhibit(Long exhibitId, Long userId, String role, ExhibitRequests.UpdateExhibitRequest request) {
        Long exhibitionId = requireExhibitExhibitionId(exhibitId);
        assertCanEdit(exhibitionId, userId, role);
        exhibitCommand.updateExhibit(exhibitId, request.slotCode(), request.placementMode(),
                toJson(request.placementJson()), request.title(), request.subtitle(),
                request.exhibitType(), request.coverUrl(), request.mediaUrl(),
                request.sourceType(), request.museumResourceId(), request.mediaAssetId(),
                request.description(), toJson(request.sourceInfo()),
                toJson(request.knowledgePoints()), request.sortOrder(), request.status());
    }

    @Override
    public void deleteExhibit(Long exhibitId, Long userId, String role) {
        Long exhibitionId = requireExhibitExhibitionId(exhibitId);
        assertCanEdit(exhibitionId, userId, role);
        exhibitCommand.deleteExhibit(exhibitId);
    }

    @Override
    public Long upsertNarration(Long exhibitId, Long userId, String role, ExhibitRequests.UpsertNarrationRequest request) {
        Long exhibitionId = requireExhibitExhibitionId(exhibitId);
        assertCanEdit(exhibitionId, userId, role);
        return exhibitCommand.upsertNarration(exhibitId, request.id(),
                request.narrationType(), request.content(), request.audioUrl(),
                request.voiceType(), request.durationSeconds(), request.sortOrder());
    }

    @Override
    public Long upsertInteraction(Long exhibitId, Long userId, String role, ExhibitRequests.UpsertInteractionRequest request) {
        Long exhibitionId = requireExhibitExhibitionId(exhibitId);
        assertCanEdit(exhibitionId, userId, role);
        return exhibitCommand.upsertInteraction(exhibitId, request.id(),
                request.interactionType(), request.questionText(),
                toJson(request.optionsJson()), request.correctAnswer(),
                request.explanation(), request.sortOrder());
    }

    @Override
    public void deleteNarration(Long narrationId, Long userId, String role) {
        exhibitCommand.deleteNarration(narrationId);
    }

    @Override
    public void deleteInteraction(Long interactionId, Long userId, String role) {
        exhibitCommand.deleteInteraction(interactionId);
    }

    private Long requireExhibitExhibitionId(Long exhibitId) {
        Long exhibitionId = exhibitQuery.findExhibitionIdByExhibitId(exhibitId);
        if (exhibitionId == null) throw new BusinessException(HttpStatus.NOT_FOUND, 40401, "展品不存在");
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
