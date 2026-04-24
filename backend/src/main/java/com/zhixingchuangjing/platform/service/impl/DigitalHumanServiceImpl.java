package com.zhixingchuangjing.platform.service.impl;

import com.zhixingchuangjing.platform.common.exception.BusinessException;
import com.zhixingchuangjing.platform.model.request.ExhibitionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.repository.DigitalHumanRepository;
import com.zhixingchuangjing.platform.repository.ExhibitionQueryRepository;
import com.zhixingchuangjing.platform.service.DigitalHumanService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DigitalHumanServiceImpl implements DigitalHumanService {

    private final DigitalHumanRepository digitalHumanRepository;
    private final ExhibitionQueryRepository exhibitionQueryRepository;

    public DigitalHumanServiceImpl(DigitalHumanRepository digitalHumanRepository,
                                   ExhibitionQueryRepository exhibitionQueryRepository) {
        this.digitalHumanRepository = digitalHumanRepository;
        this.exhibitionQueryRepository = exhibitionQueryRepository;
    }

    @Override
    @Transactional
    public ExhibitionResponses.DigitalHumanEquipmentResponse addEquipment(Long digitalHumanId,
                                                                          Long userId,
                                                                          String role,
                                                                          ExhibitionRequests.BindEquipmentRequest request) {
        Long exhibitionId = digitalHumanRepository.findExhibitionIdByDigitalHumanId(digitalHumanId);
        if (exhibitionId == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40481, "数字人不存在");
        }
        if (!exhibitionQueryRepository.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40361, "无权管理该数字人");
        }
        if (!digitalHumanRepository.museumResourceExists(request.museumResourceId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, 40081, "文博资源不存在");
        }
        Long equipmentId = digitalHumanRepository.addEquipment(
                digitalHumanId,
                request.slotCode().trim(),
                request.museumResourceId(),
                request.anchorCode(),
                request.displayOrder() == null ? 0 : request.displayOrder()
        );
        return digitalHumanRepository.findEquipmentDetail(equipmentId);
    }

    @Override
    @Transactional
    public void deleteEquipment(Long digitalHumanId, Long equipmentId, Long userId, String role) {
        Long exhibitionId = digitalHumanRepository.findExhibitionIdByDigitalHumanId(digitalHumanId);
        if (exhibitionId == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, 40481, "数字人不存在");
        }
        if (!exhibitionQueryRepository.canAccessExhibition(exhibitionId, userId, role)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, 40361, "无权管理该数字人");
        }
        digitalHumanRepository.deleteEquipment(digitalHumanId, equipmentId);
    }
}
