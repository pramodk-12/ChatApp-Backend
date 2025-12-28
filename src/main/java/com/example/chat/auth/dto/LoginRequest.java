package com.example.chat.auth.dto;

import lombok.*;

@Getter @Setter
public class LoginRequest {
    private String username;
    private String password;
}