package com.example.chat.auth.dto;

import lombok.*;

@Getter @Setter
public class SignupRequest {
    private String username;
    private String password;
    private String displayName;
}
