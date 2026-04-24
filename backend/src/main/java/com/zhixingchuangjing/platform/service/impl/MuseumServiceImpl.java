package com.zhixingchuangjing.platform.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhixingchuangjing.platform.common.api.PageResponse;
import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.common.util.PageUtils;
import com.zhixingchuangjing.platform.model.request.MuseumRequests;
import com.zhixingchuangjing.platform.model.response.MuseumResponses;
import com.zhixingchuangjing.platform.repository.MuseumRepository;
import com.zhixingchuangjing.platform.service.MuseumService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MuseumServiceImpl implements MuseumService {

    private final MuseumRepository museumRepository;
    private final ObjectMapper objectMapper;

    public MuseumServiceImpl(MuseumRepository museumRepository, ObjectMapper objectMapper) {
        this.museumRepository = museumRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<MuseumResponses.MuseumResourceResponse> getMuseumResources(String providerCode,
                                                                                   String category,
                                                                                   String keyword,
                                                                                   Integer page,
                                                                                   Integer pageSize) {
        int normalizedPage = PageUtils.normalizePage(page);
        int normalizedPageSize = PageUtils.normalizePageSize(pageSize);
        return new PageResponse<>(
                museumRepository.findResources(providerCode, category, keyword,
                        PageUtils.offset(normalizedPage, normalizedPageSize), normalizedPageSize),
                normalizedPage,
                normalizedPageSize,
                museumRepository.countResources(providerCode, category, keyword)
        );
    }

    @Override
    @Transactional
    public void syncMuseumResources(MuseumRequests.SyncMuseumResourcesRequest request) {
        Long providerId = museumRepository.findProviderId(request.providerCode());
        if (providerId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40061, "资源提供方不存在");
        }

        String category = normalizeText(request.category(), "红色文化");
        String keyword = normalizeText(request.keyword(), "数字策展");
        for (int index = 1; index <= 3; index++) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("era", "现代");
            metadata.put("difficulty", index);
            metadata.put("keyword", keyword);
            museumRepository.upsertResource(
                    providerId,
                    "%s-%s-%d".formatted(request.providerCode(), category, index),
                    "image",
                    category,
                    "%s资源样本 %d".formatted(keyword, index),
                    "知行合作馆藏",
                    "https://example.com/museum/%s/%d.jpg".formatted(request.providerCode(), index),
                    "https://example.com/museum/%s/%d".formatted(request.providerCode(), index),
                    "用于联调的文博资源样本数据，便于前端检索与数字人装备绑定。",
                    toJson(metadata)
            );
        }
    }

    @Override
    public MuseumResponses.MuseumResourceResponse getMuseumResourceDetail(Long resourceId) {
        MuseumResponses.MuseumResourceResponse detail = museumRepository.findResourceDetail(resourceId);
        if (detail == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40461, "文博资源不存在");
        }
        return detail;
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 50031, "文博资源同步失败");
        }
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
