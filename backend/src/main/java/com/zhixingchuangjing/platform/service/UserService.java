package com.zhixingchuangjing.platform.service;

import com.zhixingchuangjing.platform.model.request.UserRequests;
import com.zhixingchuangjing.platform.model.response.UserResponses;

public interface UserService {

    UserResponses.UserProfileResponse getMyProfile(Long userId);

    UserResponses.UserHomepageResponse getUserHomepage(Long targetUserId);

    java.util.List<UserResponses.PortfolioItemResponse> getMyPortfolio(Long userId);

    UserResponses.UserProfileResponse updateMyProfile(Long userId, UserRequests.UpdateProfileRequest request);

    void updateMyPassword(Long userId, UserRequests.UpdatePasswordRequest request);
}
