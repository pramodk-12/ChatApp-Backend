package com.example.chat.chat.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id","user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", insertable = false, updatable = false)
    private Chat chat;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // member role: MEMBER / ADMIN
    private String role;
}
