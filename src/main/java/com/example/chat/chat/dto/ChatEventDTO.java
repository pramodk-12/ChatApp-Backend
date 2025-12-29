package com.example.chat.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatEventDTO {
    private String type; // e.g., "DELETE_MESSAGE"
    private Long messageId;
}
