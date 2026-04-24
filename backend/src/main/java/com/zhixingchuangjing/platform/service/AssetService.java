package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.model.response.AssetResponses;
import org.springframework.web.multipart.MultipartFile;

public interface AssetService {

    AssetResponses.AssetUploadDataResponse uploadAsset(Long userId,
                                                       MultipartFile file,
                                                       String folder,
                                                       String bizType);

    PageResponse<AssetResponses.AssetResponse> getAssets(Long userId,
                                                         String assetType,
                                                         String sourceType,
                                                         Integer page,
                                                         Integer pageSize);
}
