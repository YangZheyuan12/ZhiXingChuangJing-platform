package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.model.request.MuseumRequests;
import com.zhixingchuangjing.platform.model.response.MuseumResponses;

public interface MuseumService {

    PageResponse<MuseumResponses.MuseumResourceResponse> getMuseumResources(String providerCode,
                                                                           String category,
                                                                           String keyword,
                                                                           Integer page,
                                                                           Integer pageSize);

    void syncMuseumResources(MuseumRequests.SyncMuseumResourcesRequest request);

    MuseumResponses.MuseumResourceResponse getMuseumResourceDetail(Long resourceId);
}
