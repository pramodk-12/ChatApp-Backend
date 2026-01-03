package com.example.chat.auth.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;           // Added
    private String username;    // Added
    private String displayName; // Added
    private String avatarUrl;
}