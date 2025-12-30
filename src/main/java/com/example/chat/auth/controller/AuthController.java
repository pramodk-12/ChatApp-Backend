package com.example.chat.auth.controller;

import com.example.chat.auth.dto.AuthResponse;
import com.example.chat.auth.dto.LoginRequest;
import com.example.chat.auth.dto.SignupRequest;
import com.example.chat.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody SignupRequest request) {
        return authService.register(request);
    }
}
