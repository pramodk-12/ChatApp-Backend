package com.example.chat.websocket.dto;

import lombok.Data;

@Data
public class TypingRequest {
    private Boolean isTyping;
    private String username;
}