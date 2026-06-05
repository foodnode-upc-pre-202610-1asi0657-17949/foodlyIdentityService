package com.FoodlyIdentityService.application.service;

import com.FoodlyIdentityService.application.dto.AuthResponseDto;
import com.FoodlyIdentityService.application.dto.LoginRequestDto;
import com.FoodlyIdentityService.application.dto.RegisterRequestDto;
import com.FoodlyIdentityService.application.dto.UserProfileDto;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);

    UserProfileDto getUserProfile(String userId);
}
