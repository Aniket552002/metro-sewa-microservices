package com.metrosewa.user_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private String mobileNumber;
    private String password;
}