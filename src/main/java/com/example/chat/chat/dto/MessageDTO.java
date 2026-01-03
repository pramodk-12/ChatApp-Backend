package com.example.chat.chat.dto;

import com.example.chat.chat.model.MessageStatus;
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
    private MessageStatus status;
    @Builder.Default
    private Boolean isDeleted = false;
    private Instant timestamp;
}
