package com.example.chat.chat.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String content;
    private String mediaUrl;
    private String status;
    @Builder.Default
    private Boolean isDeleted = false;
    private Instant timestamp;
}
