package com.metrosewa.user_service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String role;
}