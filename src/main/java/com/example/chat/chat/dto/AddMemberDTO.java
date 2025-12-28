package com.example.chat.chat.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddMemberDTO {
    private Long userId;
    private String role; // optional
}

