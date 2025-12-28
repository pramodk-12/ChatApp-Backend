package com.example.chat.chat.dto;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ChatMemberDTO {
    private Long userId;
    private String role;
}
