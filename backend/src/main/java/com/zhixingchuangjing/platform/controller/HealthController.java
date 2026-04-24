package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/health")
public class HealthController extends BaseController {

    @GetMapping
    public ApiResponse<Map<String, String>> health() {
        return success(Map.of("status", "UP", "service", "platform-backend"));
    }
}
