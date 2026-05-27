package com.metrosewa.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String mobileNumber;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private String role;

    private LocalDateTime createdAt;
}
