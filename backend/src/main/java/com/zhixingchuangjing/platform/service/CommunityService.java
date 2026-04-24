package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.model.request.CommunityRequests;
import com.zhixingchuangjing.platform.model.response.CommunityResponses;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;

public interface CommunityService {

    PageResponse<ExhibitionResponses.ExhibitionSummaryResponse> getCommunityExhibitions(String keyword,
                                                                                        String grade,
                                                                                        String theme,
                                                                                        String tag,
                                                                                        String sortBy,
                                                                                        Integer page,
                                                                                        Integer pageSize);

    ExhibitionResponses.ExhibitionViewerDataResponse getCommunityExhibitionDetail(Long exhibitionId);

    CommunityResponses.CommunityCommentsResponse getCommunityComments(Long exhibitionId);

    CommunityResponses.CommentResponse createComment(Long exhibitionId,
                                                    Long userId,
                                                    String role,
                                                    String nickname,
                                                    CommunityRequests.CreateCommentRequest request);

    void likeExhibition(Long exhibitionId, Long userId);

    void unlikeExhibition(Long exhibitionId, Long userId);

    void favoriteExhibition(Long exhibitionId, Long userId);

    void unfavoriteExhibition(Long exhibitionId, Long userId);

    void shareExhibition(Long exhibitionId, Long userId, CommunityRequests.ShareExhibitionRequest request);

    void featureExhibition(Long exhibitionId,
                           Long userId,
                           String role,
                           CommunityRequests.FeatureExhibitionRequest request);
}
