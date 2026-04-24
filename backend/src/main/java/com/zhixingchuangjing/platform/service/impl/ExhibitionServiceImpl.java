package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.util.PageUtils;
import com.zhixingchuangjing.platform.model.request.ExhibitionRequests;
import com.zhixingchuangjing.platform.model.response.CommunityResponses;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.repository.ExhibitionCommandRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.repository.TaskQueryRepository;
import com.zhixingchuangjing.platform.repository.UserRepository;
import com.zhixingchuangjing.platform.service.ExhibitionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ExhibitionServiceImpl implements ExhibitionService {

    private final ExhibitionQueryRepository exhibitionQueryRepository;
    private final ExhibitionCommandRepository exhibitionCommandRepository;
    private final TaskQueryRepository taskQueryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ExhibitionServiceImpl(ExhibitionQueryRepository exhibitionQueryRepository,
                                 ExhibitionCommandRepository exhibitionCommandRepository,
                                 TaskQueryRepository taskQueryRepository,
                                 UserRepository userRepository,
                                 ObjectMapper objectMapper) {
        this.exhibitionQueryRepository = exhibitionQueryRepository;
        this.exhibitionCommandRepository = exhibitionCommandRepository;
        this.taskQueryRepository = taskQueryRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ExhibitionResponses.ExhibitionDetailResponse createExhibition(Long userId,
                                                                        String role,
                                                                        ExhibitionRequests.CreateExhibitionRequest request) {
        String visibility = normalizeVisibility(request.visibility());
        if (request.taskId() != null && !taskQueryRepository.canAccessTask(request.taskId(), userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40332, "无权在该任务下创建展厅");
        }

        Long exhibitionId = exhibitionCommandRepository.createExhibition(
                userId,
                request.taskId(),
                request.title().trim(),
                normalizeText(request.coverUrl()),
                normalizeText(request.summary()),
                normalizeText(request.groupName()),
                visibility
        );
        exhibitionCommandRepository.addOwnerMember(exhibitionId, userId);
        return getExhibitionDetail(exhibitionId, userId, role);
    }

    @Override
    @Transactional
    public void updateExhibition(Long exhibitionId,
                                 Long userId,
                                 String role,
                                 ExhibitionRequests.UpdateExhibitionRequest request) {
        ExhibitionResponses.ExhibitionDetailResponse exhibition = exhibitionQueryRepository.findExhibitionDetail(exhibitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40431, "展厅不存在"));
        assertExhibitionManagePermission(exhibition, userId, role);
        exhibitionCommandRepository.updateExhibition(
                exhibitionId,
                request.title().trim(),
                normalizeText(request.coverUrl()),
                normalizeText(request.summary()),
                normalizeVisibility(request.visibility())
        );
    }

    @Override
    public PageResponse<ExhibitionResponses.ExhibitionSummaryResponse> getMyExhibitions(Long userId,
                                                                                        Long taskId,
                                                                                        String status,
                                                                                        String keyword,
                                                                                        Integer page,
                                                                                        Integer pageSize) {
        int normalizedPage = PageUtils.normalizePage(page);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return new PageResponse<>(
                exhibitionQueryRepository.findPagedMyExhibitions(userId, taskId, status, keyword,
                        PageUtils.offset(normalizedPage, normalizedPageSize), normalizedPageSize),
                normalizedPage,
                normalizedPageSize,
                exhibitionQueryRepository.countMyExhibitions(userId, taskId, status, keyword)
        );
    }

    @Override
    public ExhibitionResponses.ExhibitionDetailResponse getExhibitionDetail(Long exhibitionId, Long userId, String role) {
        assertExhibitionAccess(exhibitionId, userId, role);
        return exhibitionQueryRepository.findExhibitionDetail(exhibitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40431, "展厅不存在"));
    }

    @Override
    public List<ExhibitionResponses.ExhibitionMemberResponse> getExhibitionMembers(Long exhibitionId, Long userId, String role) {
        assertExhibitionAccess(exhibitionId, userId, role);
        return exhibitionQueryRepository.findExhibitionMembers(exhibitionId);
    }

    @Override
    @Transactional
    public void addExhibitionMembers(Long exhibitionId,
                                     Long userId,
                                     String role,
                                     ExhibitionRequests.AddExhibitionMembersRequest request) {
        ExhibitionResponses.ExhibitionDetailResponse exhibition = exhibitionQueryRepository.findExhibitionDetail(exhibitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40431, "展厅不存在"));
        assertExhibitionManagePermission(exhibition, userId, role);

        String memberRole = normalizeMemberRole(request.role());
        List<Long> memberUserIds = request.memberUserIds().stream().filter(id -> id != null).distinct().toList();
        if (memberUserIds.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40046, "至少选择一个成员");
        }
        for (Long memberUserId : memberUserIds) {
            if (!userRepository.existsById(memberUserId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, 40047, "存在无效的成员信息");
            }
        }
        exhibitionCommandRepository.addMembers(exhibitionId, memberUserIds, memberRole);
        if ("owner".equals(memberRole)) {
            exhibitionCommandRepository.updateOwner(exhibitionId, memberUserIds.get(0));
        }
    }

    @Override
    public List<ExhibitionResponses.ExhibitionVersionResponse> getVersions(Long exhibitionId, Long userId, String role) {
        assertExhibitionAccess(exhibitionId, userId, role);
        return exhibitionQueryRepository.findVersions(exhibitionId).stream()
                .map(version -> new ExhibitionResponses.ExhibitionVersionResponse(
                        version.id(),
                        version.versionNo(),
                        version.saveType(),
                        version.versionNote(),
                        version.canvasConfig(),
                        version.elementCount(),
                        parseVersionData(version.versionData()),
                        version.createdBy(),
                        version.createdAt()
                ))
                .toList();
    }

    @Override
    @Transactional
    public ExhibitionResponses.ExhibitionVersionResponse saveVersion(Long exhibitionId,
                                                                    Long userId,
                                                                    String role,
                                                                    String nickname,
                                                                    ExhibitionRequests.SaveExhibitionVersionRequest request) {
        assertExhibitionAccess(exhibitionId, userId, role);
        String saveType = normalizeSaveType(request.saveType());
        String versionDataJson = writeVersionData(request.versionData());
        int elementCount = extractElementCount(request.versionData());
        int versionNo = exhibitionCommandRepository.getNextVersionNo(exhibitionId);

        Long versionId = exhibitionCommandRepository.insertVersion(
                exhibitionId,
                versionNo,
                saveType,
                normalizeText(request.versionNote()),
                request.canvasConfig().width(),
                request.canvasConfig().height(),
                normalizeText(request.canvasConfig().background()),
                request.canvasConfig().zoom(),
                elementCount,
                versionDataJson,
                userId
        );
        exhibitionCommandRepository.updateLatestVersion(exhibitionId, versionNo);

        return new ExhibitionResponses.ExhibitionVersionResponse(
                versionId,
                versionNo,
                saveType,
                normalizeText(request.versionNote()),
                new ExhibitionResponses.CanvasConfigResponse(
                        request.canvasConfig().width(),
                        request.canvasConfig().height(),
                        normalizeText(request.canvasConfig().background()),
                        request.canvasConfig().zoom()
                ),
                elementCount,
                request.versionData(),
                buildCurrentUser(userId, role, nickname),
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void publishExhibition(Long exhibitionId,
                                  Long userId,
                                  String role,
                                  ExhibitionRequests.PublishExhibitionRequest request) {
        ExhibitionResponses.ExhibitionDetailResponse exhibition = exhibitionQueryRepository.findExhibitionDetail(exhibitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40431, "展厅不存在"));
        if (!"admin".equals(role) && !userId.equals(exhibition.ownerId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40333, "仅展厅负责人可发布展厅");
        }
        if (exhibitionCommandRepository.countVersionByNo(exhibitionId, request.versionNo()) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40041, "指定版本不存在");
        }
        exhibitionCommandRepository.publishExhibition(
                exhibitionId,
                request.versionNo(),
                normalizeVisibility(request.visibility())
        );
    }

    @Override
    @Transactional
    public ExhibitionResponses.DigitalHumanResponse upsertDigitalHuman(Long exhibitionId,
                                                                      Long userId,
                                                                      String role,
                                                                      ExhibitionRequests.UpsertDigitalHumanRequest request) {
        ExhibitionResponses.ExhibitionDetailResponse exhibition = exhibitionQueryRepository.findExhibitionDetail(exhibitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40431, "展厅不存在"));
        assertExhibitionManagePermission(exhibition, userId, role);
        exhibitionCommandRepository.upsertDigitalHuman(
                exhibitionId,
                request.name().trim(),
                normalizeText(request.avatar2dUrl()),
                normalizeText(request.model3dUrl()),
                normalizeText(request.persona()),
                normalizeText(request.voiceType()),
                normalizeText(request.storyScript()),
                writeStoryTimeline(request.storyTimeline())
        );
        return buildDigitalHuman(exhibitionId);
    }

    @Override
    public ExhibitionResponses.ExhibitionViewerDataResponse getViewerData(Long exhibitionId, Long userId, String role) {
        assertExhibitionAccess(exhibitionId, userId, role);
        ExhibitionResponses.ExhibitionDetailResponse exhibition = getExhibitionDetail(exhibitionId, userId, role);
        ExhibitionResponses.ExhibitionRenderDataResponse renderData = buildRenderData(exhibitionId);
        ExhibitionResponses.DigitalHumanResponse digitalHuman = buildDigitalHuman(exhibitionId);
        List<ExhibitionResponses.SubmissionReviewResponse> teacherReviews = exhibitionQueryRepository.findTeacherReviewsByExhibition(exhibitionId);
        List<CommunityResponses.CommentResponse> comments = exhibitionQueryRepository.findExhibitionComments(exhibitionId);

        return new ExhibitionResponses.ExhibitionViewerDataResponse(
                exhibition,
                renderData,
                digitalHuman,
                teacherReviews,
                comments
        );
    }

    @Override
    public ExhibitionResponses.DigitalHumanResponse getDigitalHuman(Long exhibitionId, Long userId, String role) {
        assertExhibitionAccess(exhibitionId, userId, role);
        return buildDigitalHuman(exhibitionId);
    }

    private ExhibitionResponses.ExhibitionRenderDataResponse buildRenderData(Long exhibitionId) {
        return exhibitionQueryRepository.findViewerVersionPayload(exhibitionId)
                .map(payload -> {
                    try {
                        JsonNode root = objectMapper.readTree(payload.versionDataJson());
                        List<ExhibitionResponses.ExhibitionElementResponse> elements = new ArrayList<>();
                        JsonNode elementNodes = root.path("elements");
                        if (elementNodes.isArray()) {
                            for (JsonNode elementNode : elementNodes) {
                                Map<String, Object> props = elementNode.path("props").isMissingNode()
                                        ? Collections.emptyMap()
                                        : objectMapper.convertValue(elementNode.path("props"), new TypeReference<>() {
                                        });
                                elements.add(new ExhibitionResponses.ExhibitionElementResponse(
                                        elementNode.path("componentType").asText(null),
                                        elementNode.path("x").asDouble(),
                                        elementNode.path("y").asDouble(),
                                        elementNode.path("width").asDouble(),
                                        elementNode.path("height").asDouble(),
                                        props
                                ));
                            }
                        }

                        return new ExhibitionResponses.ExhibitionRenderDataResponse(
                                new ExhibitionResponses.CanvasConfigResponse(
                                        payload.canvasWidth(),
                                        payload.canvasHeight(),
                                        payload.canvasBackground(),
                                        payload.zoomRatio()
                                ),
                                elements
                        );
                    } catch (IOException ex) {
                        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50010, "展厅版本数据解析失败");
                    }
                })
                .orElseGet(() -> new ExhibitionResponses.ExhibitionRenderDataResponse(
                        new ExhibitionResponses.CanvasConfigResponse(1920, 1080, "#ffffff", 1D),
                        List.of()
                ));
    }

    private ExhibitionResponses.DigitalHumanResponse buildDigitalHuman(Long exhibitionId) {
        return exhibitionQueryRepository.findDigitalHuman(exhibitionId)
                .map(payload -> {
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
                })
                .orElse(null);
    }

    private List<Map<String, Object>> parseStoryTimeline(String storyTimelineJson) throws IOException {
        if (storyTimelineJson == null || storyTimelineJson.isBlank()) {
            return List.of();
        }
        return objectMapper.readValue(storyTimelineJson, new TypeReference<>() {
        });
    }

    private void assertExhibitionAccess(Long exhibitionId, Long userId, String role) {
        if (!exhibitionQueryRepository.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40331, "无权访问该展厅");
        }
    }

    private void assertExhibitionManagePermission(ExhibitionResponses.ExhibitionDetailResponse exhibition, Long userId, String role) {
        if ("admin".equals(role) || userId.equals(exhibition.ownerId())) {
            return;
        }
        throw new BusinessException(HttpStatus.FORBIDDEN, 40334, "无权管理该展厅");
    }

    private int extractElementCount(Object versionData) {
        JsonNode root = objectMapper.valueToTree(versionData);
        JsonNode elementsNode = root.path("elements");
        return elementsNode.isArray() ? elementsNode.size() : 0;
    }

    private String writeVersionData(Object versionData) {
        try {
            return objectMapper.writeValueAsString(versionData);
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40042, "版本数据格式不合法");
        }
    }

    private Object parseVersionData(Object versionData) {
        if (!(versionData instanceof String json)) {
            return versionData;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (IOException ex) {
            return json;
        }
    }

    private CommonResponses.SimpleUser buildCurrentUser(Long userId, String role, String nickname) {
        return new CommonResponses.SimpleUser(userId, role, nickname, nickname, null);
    }

    private String writeStoryTimeline(List<ExhibitionRequests.StoryTimelineItemRequest> storyTimeline) {
        try {
            return objectMapper.writeValueAsString(storyTimeline == null ? List.of() : storyTimeline);
        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40048, "数字人时间线格式不合法");
        }
    }

    private String normalizeVisibility(String visibility) {
        String normalized = normalizeText(visibility);
        if (normalized == null) {
            return "class";
        }
        String result = normalized.toLowerCase(Locale.ROOT);
        if (!Set.of("private", "class", "public").contains(result)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40043, "展厅可见性不合法");
        }
        return result;
    }

    private String normalizeSaveType(String saveType) {
        String normalized = normalizeText(saveType);
        if (normalized == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40044, "保存类型不能为空");
        }
        String result = normalized.toLowerCase(Locale.ROOT);
        if (!Set.of("manual", "autosave", "publish").contains(result)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40045, "保存类型不合法");
        }
        return result;
    }

    private String normalizeMemberRole(String role) {
        String normalized = normalizeText(role);
        if (normalized == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40049, "成员角色不能为空");
        }
        String result = normalized.toLowerCase(Locale.ROOT);
        if (!Set.of("owner", "editor", "viewer").contains(result)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40050, "成员角色不合法");
        }
        return result;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
