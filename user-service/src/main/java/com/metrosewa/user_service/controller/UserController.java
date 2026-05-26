package com.metrosewa.user_service.controller;

import com.metrosewa.user_service.dto.LoginRequest;
import com.metrosewa.user_service.dto.LoginResponse;
import com.metrosewa.user_service.dto.UserRegisterRequest;
import com.metrosewa.user_service.dto.UserResponse;
import com.metrosewa.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody UserRegisterRequest request) {
        // Creates a new passenger account.
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // Checks mobile number and password, then returns a JWT token.
        return userService.login(request);
    }

    @GetMapping("/profile/{mobileNumber}")
    public UserResponse getProfile(@PathVariable String mobileNumber) {
        // Returns profile details without exposing the saved password.
        return userService.getUserByMobileNumber(mobileNumber);
    }
}
