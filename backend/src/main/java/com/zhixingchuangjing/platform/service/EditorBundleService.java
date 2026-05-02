package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.EditorBundleRequests;
import com.zhixingchuangjing.platform.model.response.EditorBundleResponses;

public interface EditorBundleService {
    EditorBundleResponses.EditorBundleResponse getBundle(Long exhibitionId, Long userId, String role);
    EditorBundleResponses.SaveBundleResultResponse saveBundle(Long exhibitionId, Long userId, String role,
        EditorBundleRequests.SaveBundleRequest request);
    void saveDraftBundle(Long exhibitionId, Long userId, String role,
        EditorBundleRequests.DraftBundleRequest request);
}
