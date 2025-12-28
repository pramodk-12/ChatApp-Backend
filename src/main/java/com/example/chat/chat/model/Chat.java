package com.example.chat.chat.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "chats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Chat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "PRIVATE" or "GROUP"
    @Column(nullable = false)
    private String type;

    private String name; // for group chats

    private Long createdBy;

    private Instant createdAt;

    @OneToMany(mappedBy = "chat")
    private Set<ChatMember> members;

    // optional: last message preview fields for fast query
    private Long lastMessageId;
    private Long lastMessageTimestamp;
}

