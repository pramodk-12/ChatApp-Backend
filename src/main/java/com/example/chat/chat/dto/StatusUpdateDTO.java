package com.example.chat.chat.dto;

import com.example.chat.chat.model.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateDTO {
    private Long chatId;
    private Long readerId;      // Who read the messages
    private MessageStatus status; // Usually 'READ'
}