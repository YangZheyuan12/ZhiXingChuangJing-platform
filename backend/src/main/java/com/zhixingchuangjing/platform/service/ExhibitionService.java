package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.model.request.ExhibitionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;

import java.util.List;

public interface ExhibitionService {

    ExhibitionResponses.ExhibitionDetailResponse createExhibition(Long userId,
                                                                  String role,
                                                                  ExhibitionRequests.CreateExhibitionRequest request);

    void updateExhibition(Long exhibitionId,
                          Long userId,
                          String role,
                          ExhibitionRequests.UpdateExhibitionRequest request);

    PageResponse<ExhibitionResponses.ExhibitionSummaryResponse> getMyExhibitions(Long userId,
                                                                                 Long taskId,
                                                                                 String status,
                                                                                 String keyword,
                                                                                 Integer page,
                                                                                 Integer pageSize);

    ExhibitionResponses.ExhibitionDetailResponse getExhibitionDetail(Long exhibitionId, Long userId, String role);

    List<ExhibitionResponses.ExhibitionMemberResponse> getExhibitionMembers(Long exhibitionId, Long userId, String role);

    void addExhibitionMembers(Long exhibitionId,
                              Long userId,
                              String role,
                              ExhibitionRequests.AddExhibitionMembersRequest request);

    List<ExhibitionResponses.ExhibitionVersionResponse> getVersions(Long exhibitionId, Long userId, String role);

    ExhibitionResponses.ExhibitionVersionResponse saveVersion(Long exhibitionId,
                                                             Long userId,
                                                             String role,
                                                             String nickname,
                                                             ExhibitionRequests.SaveExhibitionVersionRequest request);

    void publishExhibition(Long exhibitionId,
                           Long userId,
                           String role,
                           ExhibitionRequests.PublishExhibitionRequest request);

    ExhibitionResponses.DigitalHumanResponse upsertDigitalHuman(Long exhibitionId,
                                                                Long userId,
                                                                String role,
                                                                ExhibitionRequests.UpsertDigitalHumanRequest request);

    ExhibitionResponses.ExhibitionViewerDataResponse getViewerData(Long exhibitionId, Long userId, String role);

    ExhibitionResponses.DigitalHumanResponse getDigitalHuman(Long exhibitionId, Long userId, String role);
}
