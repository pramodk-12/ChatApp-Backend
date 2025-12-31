package com.example.chat.chat.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatDTO {
    private Long id;
    private String type;
    private boolean isReadOnly;
    private String avatarUrl;
    private String name;
    private Long createdBy;
    private Instant createdAt;
    private List<ChatMemberDTO> members;
    private Long lastMessageId;
    private Instant lastMessageTimestamp;
}

