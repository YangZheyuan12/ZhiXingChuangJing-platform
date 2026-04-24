package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.ExhibitionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;

public interface DigitalHumanService {

    ExhibitionResponses.DigitalHumanEquipmentResponse addEquipment(Long digitalHumanId,
                                                                   Long userId,
                                                                   String role,
                                                                   ExhibitionRequests.BindEquipmentRequest request);

    void deleteEquipment(Long digitalHumanId, Long equipmentId, Long userId, String role);
}
