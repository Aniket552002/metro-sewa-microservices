package com.metrosewa.user_service.service;

import com.metrosewa.user_service.dto.LoginRequest;
import com.metrosewa.user_service.dto.LoginResponse;
import com.metrosewa.user_service.dto.UserRegisterRequest;
import com.metrosewa.user_service.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getUserByMobileNumber(String mobileNumber);
}
