package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.service.ExhibitionTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/exhibition-templates")
public class ExhibitionTemplateController extends BaseController {

    private final ExhibitionTemplateService exhibitionTemplateService;

    public ExhibitionTemplateController(ExhibitionTemplateService exhibitionTemplateService) {
        this.exhibitionTemplateService = exhibitionTemplateService;
    }

    @GetMapping
    public ApiResponse<List<ExhibitionResponses.ExhibitionTemplateResponse>> listTemplates() {
        return success(exhibitionTemplateService.listActiveTemplates());
    }
}
