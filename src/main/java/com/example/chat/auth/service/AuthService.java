package com.example.chat.auth.service;

import com.example.chat.auth.dto.AuthResponse;
import com.example.chat.auth.dto.LoginRequest;
import com.example.chat.auth.dto.SignupRequest;
import com.example.chat.auth.models.User;
import com.example.chat.auth.repositories.UserRepository;
import com.example.chat.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    public AuthResponse login(LoginRequest request) {
        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!encoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid username or password");

        String token = jwtService.generateToken(user.getUsername());

        // Return the full user context
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getDisplayName());
    }

    @Transactional
    public AuthResponse register(SignupRequest request) {
        if (repository.findByUsername(request.getUsername()).isPresent())
            throw new RuntimeException("Username already taken");

        User user = User.builder()
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .build();

        User savedUser = repository.save(user);
        String token = jwtService.generateToken(savedUser.getUsername());

        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getDisplayName());
    }
}