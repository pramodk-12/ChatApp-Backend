package com.example.chat.chat.dto;

import lombok.*;

import java.time.Instant;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MessageDTO {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String content;
    private String mediaUrl;
    private String status;
    private Instant timestamp;
}
