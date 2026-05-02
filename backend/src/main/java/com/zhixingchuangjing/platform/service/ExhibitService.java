package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.ExhibitRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitResponses;

import java.util.List;

public interface ExhibitService {
    List<ExhibitResponses.ExhibitSummaryResponse> listExhibits(Long zoneId, Long userId, String role);
    ExhibitResponses.ExhibitResponse getExhibitDetail(Long exhibitId, Long userId, String role);
    Long createExhibit(Long exhibitionId, Long userId, String role, ExhibitRequests.CreateExhibitRequest request);
    void updateExhibit(Long exhibitId, Long userId, String role, ExhibitRequests.UpdateExhibitRequest request);
    void deleteExhibit(Long exhibitId, Long userId, String role);
    Long upsertNarration(Long exhibitId, Long userId, String role, ExhibitRequests.UpsertNarrationRequest request);
    Long upsertInteraction(Long exhibitId, Long userId, String role, ExhibitRequests.UpsertInteractionRequest request);
    void deleteNarration(Long narrationId, Long userId, String role);
    void deleteInteraction(Long interactionId, Long userId, String role);
}
