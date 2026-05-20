package com.metrosewa.user_service.service;

import com.metrosewa.user_service.dto.LoginRequest;
import com.metrosewa.user_service.dto.LoginResponse;
import com.metrosewa.user_service.dto.UserRegisterRequest;
import com.metrosewa.user_service.dto.UserResponse;
import com.metrosewa.user_service.entity.User;
import com.metrosewa.user_service.repository.UserRepository;
import com.metrosewa.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse registerUser(UserRegisterRequest request) {

        if (userRepository.findByMobileNumber(request.getMobileNumber()).isPresent()) {
            throw new RuntimeException("Mobile number already registered");
        }

        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .mobileNumber(savedUser.getMobileNumber())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new RuntimeException("Invalid mobile number"));

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getMobileNumber());

        return new LoginResponse(token);
    }

    @Override
    public UserResponse getUserByMobileNumber(String mobileNumber) {

        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .mobileNumber(user.getMobileNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
