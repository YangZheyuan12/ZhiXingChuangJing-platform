package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.request.ExhibitionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.repository.DigitalHumanRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionCommandRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.service.DigitalHumanService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DigitalHumanServiceImpl implements DigitalHumanService {

    private final DigitalHumanRepository digitalHumanRepository;
    private final ExhibitionQueryRepository exhibitionQueryRepository;
    private final ExhibitionCommandRepository exhibitionCommandRepository;
    private final ObjectMapper objectMapper;

    public DigitalHumanServiceImpl(DigitalHumanRepository digitalHumanRepository,
                                   ExhibitionQueryRepository exhibitionQueryRepository,
                                   ExhibitionCommandRepository exhibitionCommandRepository,
                                   ObjectMapper objectMapper) {
        this.digitalHumanRepository = digitalHumanRepository;
        this.exhibitionQueryRepository = exhibitionQueryRepository;
        this.exhibitionCommandRepository = exhibitionCommandRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ExhibitionResponses.DigitalHumanEquipmentResponse addEquipment(Long digitalHumanId,
                                                                          Long userId,
                                                                          String role,
                                                                          ExhibitionRequests.BindEquipmentRequest request) {
        Long exhibitionId = digitalHumanRepository.findExhibitionIdByDigitalHumanId(digitalHumanId);
        if (exhibitionId == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40481, "数字人不存在");
        }
        if (!exhibitionQueryRepository.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40361, "无权管理该数字人");
        }
        if (!digitalHumanRepository.museumResourceExists(request.museumResourceId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40081, "文博资源不存在");
        }
        Long equipmentId = digitalHumanRepository.addEquipment(
                digitalHumanId,
                request.slotCode().trim(),
                request.museumResourceId(),
                request.anchorCode(),
                request.displayOrder() == null ? 0 : request.displayOrder()
        );
        return digitalHumanRepository.findEquipmentDetail(equipmentId);
    }

    @Override
    @Transactional
    public void deleteEquipment(Long digitalHumanId, Long equipmentId, Long userId, String role) {
        Long exhibitionId = digitalHumanRepository.findExhibitionIdByDigitalHumanId(digitalHumanId);
        if (exhibitionId == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40481, "数字人不存在");
        }
        if (!exhibitionQueryRepository.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40361, "无权管理该数字人");
        }
        digitalHumanRepository.deleteEquipment(digitalHumanId, equipmentId);
    }

    @Override
    @Transactional
    public ExhibitionResponses.DigitalHumanResponse updateDigitalHuman(Long digitalHumanId,
                                                                       Long userId,
                                                                       String role,
                                                                       ExhibitionRequests.UpsertDigitalHumanRequest request) {
        var payload = exhibitionQueryRepository.findDigitalHumanById(digitalHumanId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40481, "数字人不存在"));
        if (!exhibitionQueryRepository.canAccessExhibition(payload.exhibitionId(), userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40361, "无权管理该数字人");
        }
        exhibitionCommandRepository.updateDigitalHuman(
                digitalHumanId,
                request.name().trim(),
                normalizeText(request.avatar2dUrl()),
                normalizeText(request.model3dUrl()),
                normalizeText(request.persona()),
                normalizeText(request.voiceType()),
                normalizeText(request.storyScript()),
                writeStoryTimeline(request.storyTimeline())
        );
        return exhibitionQueryRepository.findDigitalHumanById(digitalHumanId)
                .map(this::buildDigitalHumanFromPayload)
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50012, "数字人更新后查询失败"));
    }

    @Override
    @Transactional
    public void deleteDigitalHuman(Long digitalHumanId, Long userId, String role) {
        var payload = exhibitionQueryRepository.findDigitalHumanById(digitalHumanId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40481, "数字人不存在"));
        if (!exhibitionQueryRepository.canAccessExhibition(payload.exhibitionId(), userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40361, "无权管理该数字人");
        }
        int referencingZones = exhibitionCommandRepository.countZonesUsingDigitalHuman(digitalHumanId);
        if (referencingZones > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, 40901,
                    "该数字人仍被 " + referencingZones + " 个展区引用，请先撤下后再删除");
        }
        exhibitionCommandRepository.deleteDigitalHuman(digitalHumanId);
    }

    private ExhibitionResponses.DigitalHumanResponse buildDigitalHumanFromPayload(
            ExhibitionQueryRepository.DigitalHumanPayload payload) {
        try {
            List<Map<String, Object>> storyTimeline = parseStoryTimeline(payload.storyTimelineJson());
            List<ExhibitionResponses.DigitalHumanEquipmentResponse> items =
                    exhibitionQueryRepository.findDigitalHumanEquipments(payload.id());
            return new ExhibitionResponses.DigitalHumanResponse(
                    payload.id(),
                    payload.exhibitionId(),
                    payload.name(),
                    payload.avatar2dUrl(),
                    payload.model3dUrl(),
                    payload.persona(),
                    payload.voiceType(),
                    payload.storyScript(),
                    storyTimeline,
                    items
            );
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50011, "数字人时间线解析失败");
        }
    }

    private List<Map<String, Object>> parseStoryTimeline(String storyTimelineJson) throws IOException {
        if (storyTimelineJson == null || storyTimelineJson.isBlank()) {
            return List.of();
        }
        return objectMapper.readValue(storyTimelineJson, new TypeReference<>() {
        });
    }

    private String writeStoryTimeline(List<ExhibitionRequests.StoryTimelineItemRequest> storyTimeline) {
        try {
            return objectMapper.writeValueAsString(storyTimeline == null ? List.of() : storyTimeline);
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40043, "故事时间线格式不合法");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
