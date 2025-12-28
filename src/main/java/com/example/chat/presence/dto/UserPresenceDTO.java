package com.example.chat.presence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPresenceDTO {
    private Long userId;
    private String username;
    private boolean online;
    private Long lastSeenTimestamp;
}