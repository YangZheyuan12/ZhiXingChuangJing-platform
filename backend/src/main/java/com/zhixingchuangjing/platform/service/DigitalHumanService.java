package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.ExhibitionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;

public interface DigitalHumanService {

    ExhibitionResponses.DigitalHumanEquipmentResponse addEquipment(Long digitalHumanId,
                                                                   Long userId,
                                                                   String role,
                                                                   ExhibitionRequests.BindEquipmentRequest request);

    void deleteEquipment(Long digitalHumanId, Long equipmentId, Long userId, String role);

    /** 更新指定数字人角色信息（不变更归属展厅）。 */
    ExhibitionResponses.DigitalHumanResponse updateDigitalHuman(Long digitalHumanId,
                                                                Long userId,
                                                                String role,
                                                                ExhibitionRequests.UpsertDigitalHumanRequest request);

    /** 删除数字人角色。删除前会校验是否被任何展区引用，若被引用则拒绝。 */
    void deleteDigitalHuman(Long digitalHumanId, Long userId, String role);
}
