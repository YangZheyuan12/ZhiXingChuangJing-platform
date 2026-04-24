package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.model.request.MuseumRequests;
import com.zhixingchuangjing.platform.model.response.MuseumResponses;
import com.zhixingchuangjing.platform.service.MuseumService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/museum/resources")
public class MuseumController extends BaseController {

    private final MuseumService museumService;

    public MuseumController(MuseumService museumService) {
        this.museumService = museumService;
    }

    @GetMapping
    public ApiResponse<PageResponse<MuseumResponses.MuseumResourceResponse>> getMuseumResources(
            @RequestParam(required = false) String providerCode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return success(museumService.getMuseumResources(providerCode, category, keyword, page, pageSize));
    }

    @PostMapping
    public ApiResponse<Void> syncMuseumResources(@Valid @RequestBody MuseumRequests.SyncMuseumResourcesRequest request) {
        museumService.syncMuseumResources(request);
        return successMessage("文博资源同步成功");
    }

    @GetMapping("/{resourceId}")
    public ApiResponse<MuseumResponses.MuseumResourceResponse> getMuseumResourceDetail(@PathVariable Long resourceId) {
        return success(museumService.getMuseumResourceDetail(resourceId));
    }
}
