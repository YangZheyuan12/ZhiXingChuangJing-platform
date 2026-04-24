package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.util.PageUtils;
import com.zhixingchuangjing.platform.model.request.CommunityRequests;
import com.zhixingchuangjing.platform.model.response.CommunityResponses;
import com.zhixingchuangjing.platform.model.response.CommonResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.repository.CommunityCommandRepository;
import com.zhixingchuangjing.platform.repository.CommunityQueryRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.repository.TaskQueryRepository;
import com.zhixingchuangjing.platform.service.CommunityService;
import com.zhixingchuangjing.platform.service.ExhibitionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityServiceImpl implements CommunityService {

    private final CommunityQueryRepository communityQueryRepository;
    private final CommunityCommandRepository communityCommandRepository;
    private final ExhibitionQueryRepository exhibitionQueryRepository;
    private final ExhibitionService exhibitionService;
    private final TaskQueryRepository taskQueryRepository;

    public CommunityServiceImpl(CommunityQueryRepository communityQueryRepository,
                                CommunityCommandRepository communityCommandRepository,
                                ExhibitionQueryRepository exhibitionQueryRepository,
                                ExhibitionService exhibitionService,
                                TaskQueryRepository taskQueryRepository) {
        this.communityQueryRepository = communityQueryRepository;
        this.communityCommandRepository = communityCommandRepository;
        this.exhibitionQueryRepository = exhibitionQueryRepository;
        this.exhibitionService = exhibitionService;
        this.taskQueryRepository = taskQueryRepository;
    }

    @Override
    public PageResponse<ExhibitionResponses.ExhibitionSummaryResponse> getCommunityExhibitions(String keyword,
                                                                                               String grade,
                                                                                               String theme,
                                                                                               String tag,
                                                                                               String sortBy,
                                                                                               Integer page,
                                                                                               Integer pageSize) {
        int normalizedPage = PageUtils.normalizePage(page);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return new PageResponse<>(
                communityQueryRepository.findPagedCommunityExhibitions(keyword, grade, theme, tag, sortBy,
                        PageUtils.offset(normalizedPage, normalizedPageSize), normalizedPageSize),
                normalizedPage,
                normalizedPageSize,
                communityQueryRepository.countCommunityExhibitions(keyword, grade, theme, tag, sortBy)
        );
    }

    @Override
    public ExhibitionResponses.ExhibitionViewerDataResponse getCommunityExhibitionDetail(Long exhibitionId) {
        assertPublicExhibition(exhibitionId);
        return exhibitionService.getViewerData(exhibitionId, 0L, "admin");
    }

    @Override
    public CommunityResponses.CommunityCommentsResponse getCommunityComments(Long exhibitionId) {
        assertPublicExhibition(exhibitionId);
        return new CommunityResponses.CommunityCommentsResponse(
                exhibitionQueryRepository.findTeacherReviewsByExhibition(exhibitionId),
                exhibitionQueryRepository.findExhibitionComments(exhibitionId)
        );
    }

    @Override
    @Transactional
    public CommunityResponses.CommentResponse createComment(Long exhibitionId,
                                                            Long userId,
                                                            String role,
                                                            String nickname,
                                                            CommunityRequests.CreateCommentRequest request) {
        assertPublicExhibition(exhibitionId);
        ExhibitionResponses.ExhibitionDetailResponse exhibition = exhibitionQueryRepository.findExhibitionDetail(exhibitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40441, "社区展厅不存在"));

        Long rootCommentId = null;
        if (request.parentCommentId() != null) {
            if (communityCommandRepository.countCommentById(exhibitionId, request.parentCommentId()) == 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, 40051, "父评论不存在");
            }
            rootCommentId = communityCommandRepository.findRootCommentId(request.parentCommentId());
        }

        Long commentId = communityCommandRepository.createComment(
                exhibitionId,
                userId,
                request.parentCommentId(),
                rootCommentId,
                request.content().trim()
        );

        List<Long> mentionUserIds = request.mentionUserIds() == null
                ? List.of()
                : request.mentionUserIds().stream()
                .filter(id -> id != null && !id.equals(userId))
                .distinct()
                .toList();
        communityCommandRepository.createMentions(commentId, mentionUserIds);
        communityCommandRepository.increaseCommentCount(exhibitionId);

        if (!userId.equals(exhibition.ownerId())) {
            communityCommandRepository.createNotification(
                    exhibition.ownerId(),
                    "comment",
                    "展厅收到新评论",
                    "%s 评论了你的展厅《%s》".formatted(nickname, exhibition.title()),
                    "exhibition_comment",
                    commentId
            );
        }

        return new CommunityResponses.CommentResponse(
                commentId,
                exhibitionId,
                userId,
                request.parentCommentId(),
                rootCommentId,
                request.content().trim(),
                "normal",
                new CommonResponses.SimpleUser(userId, role, nickname, nickname, null),
                List.of(),
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void likeExhibition(Long exhibitionId, Long userId) {
        assertPublicExhibition(exhibitionId);
        if (communityCommandRepository.hasInteraction(exhibitionId, userId, "like", "")) {
            return;
        }
        communityCommandRepository.addInteraction(exhibitionId, userId, "like", "");
        communityCommandRepository.updateInteractionCount(exhibitionId, "like_count", 1);
        notifyOwner(exhibitionId, userId, "like", "展厅收到新点赞");
    }

    @Override
    @Transactional
    public void unlikeExhibition(Long exhibitionId, Long userId) {
        assertPublicExhibition(exhibitionId);
        if (!communityCommandRepository.hasInteraction(exhibitionId, userId, "like", "")) {
            return;
        }
        communityCommandRepository.removeInteraction(exhibitionId, userId, "like");
        communityCommandRepository.updateInteractionCount(exhibitionId, "like_count", -1);
    }

    @Override
    @Transactional
    public void favoriteExhibition(Long exhibitionId, Long userId) {
        assertPublicExhibition(exhibitionId);
        if (communityCommandRepository.hasInteraction(exhibitionId, userId, "favorite", "")) {
            return;
        }
        communityCommandRepository.addInteraction(exhibitionId, userId, "favorite", "");
        communityCommandRepository.updateInteractionCount(exhibitionId, "favorite_count", 1);
        notifyOwner(exhibitionId, userId, "favorite", "展厅收到新收藏");
    }

    @Override
    @Transactional
    public void unfavoriteExhibition(Long exhibitionId, Long userId) {
        assertPublicExhibition(exhibitionId);
        if (!communityCommandRepository.hasInteraction(exhibitionId, userId, "favorite", "")) {
            return;
        }
        communityCommandRepository.removeInteraction(exhibitionId, userId, "favorite");
        communityCommandRepository.updateInteractionCount(exhibitionId, "favorite_count", -1);
    }

    @Override
    @Transactional
    public void shareExhibition(Long exhibitionId, Long userId, CommunityRequests.ShareExhibitionRequest request) {
        assertPublicExhibition(exhibitionId);
        if (communityCommandRepository.hasInteraction(exhibitionId, userId, "share", request.channel().trim())) {
            return;
        }
        communityCommandRepository.addInteraction(exhibitionId, userId, "share", request.channel().trim());
    }

    @Override
    @Transactional
    public void featureExhibition(Long exhibitionId,
                                  Long userId,
                                  String role,
                                  CommunityRequests.FeatureExhibitionRequest request) {
        assertPublicExhibition(exhibitionId);
        ExhibitionResponses.ExhibitionDetailResponse exhibition = exhibitionQueryRepository.findExhibitionDetail(exhibitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40441, "社区展厅不存在"));
        boolean canFeature = "admin".equals(role)
                || ("teacher".equals(role) && exhibition.taskId() != null
                && taskQueryRepository.canManageTaskProgress(exhibition.taskId(), userId, role));
        if (!canFeature) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40341, "无权推荐该展厅");
        }
        boolean featured = Boolean.TRUE.equals(request.featured());
        communityCommandRepository.setFeatured(exhibitionId, featured, featured ? request.featuredReason() : null);
    }

    private void assertPublicExhibition(Long exhibitionId) {
        if (!exhibitionQueryRepository.isPublicPublishedExhibition(exhibitionId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40441, "社区展厅不存在");
        }
    }

    private void notifyOwner(Long exhibitionId, Long userId, String interactionType, String title) {
        ExhibitionResponses.ExhibitionDetailResponse exhibition = exhibitionQueryRepository.findExhibitionDetail(exhibitionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, 40441, "社区展厅不存在"));
        if (userId.equals(exhibition.ownerId())) {
            return;
        }
        communityCommandRepository.createNotification(
                exhibition.ownerId(),
                interactionType,
                title,
                "你的展厅《%s》收到了新的互动".formatted(exhibition.title()),
                "exhibition",
                exhibitionId
        );
    }
}
