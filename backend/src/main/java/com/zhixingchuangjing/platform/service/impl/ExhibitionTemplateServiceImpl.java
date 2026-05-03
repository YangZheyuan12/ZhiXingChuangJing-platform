package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.service.ExhibitionTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExhibitionTemplateServiceImpl implements ExhibitionTemplateService {

    private final ExhibitionQueryRepository exhibitionQueryRepository;

    public ExhibitionTemplateServiceImpl(ExhibitionQueryRepository exhibitionQueryRepository) {
        this.exhibitionQueryRepository = exhibitionQueryRepository;
    }

    @Override
    public List<ExhibitionResponses.ExhibitionTemplateResponse> listActiveTemplates() {
        return exhibitionQueryRepository.listActiveTemplates();
    }
}
