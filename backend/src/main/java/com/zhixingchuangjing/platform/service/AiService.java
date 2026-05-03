package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.AiRequests;
import com.zhixingchuangjing.platform.model.response.AiResponses;

public interface AiService {

    AiResponses.GenerateNarrationResponse generateNarration(AiRequests.GenerateNarrationRequest request);
}
