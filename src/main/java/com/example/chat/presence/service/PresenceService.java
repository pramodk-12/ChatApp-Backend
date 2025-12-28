package com.example.chat.presence.service;

import com.example.chat.auth.models.User;
import com.example.chat.auth.repositories.UserRepository;
import com.example.chat.presence.dto.UserPresenceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    private static final String KEY_PREFIX = "presence:user:";

    public void markOnline(Long userId) {
        String key = KEY_PREFIX + userId;
        // Set the key with a 60-second expiration
        // Every time this is called, the timer resets to 60 seconds
        redisTemplate.opsForValue().set(key, "online", 60, TimeUnit.SECONDS);
    }

    public void markOffline(Long userId) {
        // Manually delete the key on logout
        redisTemplate.delete(KEY_PREFIX + userId);
    }

    public List<UserPresenceDTO> getAllPresenceStatus() {
        // 1. Get all keys starting with our prefix
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");

        // 2. Map existing keys to Online status
        // Note: For a list of ALL users (including offline),
        // fetch from DB and check redis.hasKey(key) for each.
        return userRepository.findAll().stream().map(user -> {
            boolean isOnline = Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + user.getId()));
            return new UserPresenceDTO(user.getId(), user.getUsername(), isOnline, null);
        }).collect(Collectors.toList());
    }
}
