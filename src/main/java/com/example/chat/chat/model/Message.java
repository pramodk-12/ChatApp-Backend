package com.example.chat.chat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_chat_timestamp", columnList = "chat_id, timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String mediaUrl;

    @Builder.Default
    private boolean isDeleted = false;

    private String status; // SENT / DELIVERED / READ

    private Instant timestamp;
}

