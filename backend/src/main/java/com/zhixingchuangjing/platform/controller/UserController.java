package com.zhixingchuangjing.platform.controller;

import com.zhixingchuangjing.platform.common.api.ApiResponse;
import com.zhixingchuangjing.platform.common.api.BaseController;
import com.zhixingchuangjing.platform.common.constant.ApiPathConstants;
import com.zhixingchuangjing.platform.common.security.SecurityUserDetails;
import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.UserResponses;
import com.zhixingchuangjing.platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPathConstants.API_V1 + "/users")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponses.UserProfileResponse> getMyProfile(@AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(userService.getMyProfile(currentUser.getId()));
    }

    @GetMapping("/{userId}/homepage")
    public ApiResponse<UserResponses.UserHomepageResponse> getUserHomepage(@PathVariable Long userId) {
        return success(userService.getUserHomepage(userId));
    }

    @GetMapping("/me/portfolio")
    public ApiResponse<java.util.List<UserResponses.PortfolioItemResponse>> getMyPortfolio(
            @AuthenticationPrincipal SecurityUserDetails currentUser) {
        return success(userService.getMyPortfolio(currentUser.getId()));
    }

    @PutMapping("/me/profile")
    public ApiResponse<UserResponses.UserProfileResponse> updateMyProfile(@AuthenticationPrincipal SecurityUserDetails currentUser,
                                                                          @Valid @RequestBody UserRequests.UpdateProfileRequest request) {
        return success(userService.updateMyProfile(currentUser.getId(), request));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> updateMyPassword(@AuthenticationPrincipal SecurityUserDetails currentUser,
                                              @Valid @RequestBody UserRequests.UpdatePasswordRequest request) {
        userService.updateMyPassword(currentUser.getId(), request);
        return successMessage("密码修改成功");
    }
}
