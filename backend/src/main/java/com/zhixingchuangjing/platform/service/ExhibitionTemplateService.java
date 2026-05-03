package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;

import java.util.List;

public interface ExhibitionTemplateService {

    List<ExhibitionResponses.ExhibitionTemplateResponse> listActiveTemplates();
}
