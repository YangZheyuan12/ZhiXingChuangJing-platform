package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.model.request.AiRequests;
import com.zhixingchuangjing.platform.model.response.AiResponses;
import com.zhixingchuangjing.platform.service.AiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/ai")
public class AiController extends BaseController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/narration")
    public ApiResponse<AiResponses.GenerateNarrationResponse> generateNarration(
            @Valid @RequestBody AiRequests.GenerateNarrationRequest request) {
        return success(aiService.generateNarration(request));
    }
}
