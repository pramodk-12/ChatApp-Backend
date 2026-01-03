package com.example.chat.auth.controller;

import com.example.chat.auth.models.User;
import com.example.chat.auth.repositories.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        // Fetch all users from the auth table
        List<User> users = userRepository.findAll();

        // Convert to a simple DTO (never send passwords!)
        List<UserSummary> dtos = users.stream()
                .map(u -> new UserSummary(u.getId(), u.getUsername()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        // 1. Get currently logged-in user
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Update fields if they are provided
        if (request.getAvatarUrl() != null) {
            currentUser.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getDisplayName() != null) {
            currentUser.setDisplayName(request.getDisplayName());
        }

        // 3. Save
        User updatedUser = userRepository.save(currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    @Data
    public static class UpdateProfileRequest {
        private String displayName;
        private String avatarUrl;
    }

    // Inner record/class for the DTO
    public record UserSummary(Long id, String username) {}
}