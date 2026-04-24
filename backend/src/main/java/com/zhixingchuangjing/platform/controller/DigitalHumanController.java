package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.ExhibitionRequests;
import com.zhixingchuangjing.platform.model.response.ExhibitionResponses;
import com.zhixingchuangjing.platform.service.DigitalHumanService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/digital-humans")
public class DigitalHumanController extends BaseController {

    private final DigitalHumanService digitalHumanService;

    public DigitalHumanController(DigitalHumanService digitalHumanService) {
        this.digitalHumanService = digitalHumanService;
    }

    @PostMapping("/{digitalHumanId}/equipments")
    public ApiResponse<ExhibitionResponses.DigitalHumanEquipmentResponse> addEquipment(
            @PathVariable Long digitalHumanId,
            @AuthenticationPrincipal SecurityUserDetails currentUser,
            @Valid @RequestBody ExhibitionRequests.BindEquipmentRequest request) {
        return success(digitalHumanService.addEquipment(digitalHumanId, currentUser.getId(), currentUser.getRole(), request));
    }

    @DeleteMapping("/{digitalHumanId}/equipments/{equipmentId}")
    public ApiResponse<Void> deleteEquipment(@PathVariable Long digitalHumanId,
                                             @PathVariable Long equipmentId,
                                             @AuthenticationPrincipal SecurityUserDetails currentUser) {
        digitalHumanService.deleteEquipment(digitalHumanId, equipmentId, currentUser.getId(), currentUser.getRole());
        return successMessage("装备删除成功");
    }
}
