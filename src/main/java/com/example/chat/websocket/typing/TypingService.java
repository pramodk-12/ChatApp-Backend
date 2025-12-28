package com.example.chat.websocket.typing;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TypingService {

    private final RedisTemplate<String, String> redisTemplate;

    public void markTyping(Long chatId, Long userId) {
        String key = "typing:" + chatId + ":" + userId;
        redisTemplate.opsForValue().set(key, "1", 3, TimeUnit.SECONDS);
    }

    public boolean isTyping(Long chatId, Long userId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("typing:" + chatId + ":" + userId)
        );
    }
}
