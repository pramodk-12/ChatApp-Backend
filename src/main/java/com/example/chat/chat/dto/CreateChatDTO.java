package com.example.chat.chat.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateChatDTO {
    private String type; // PRIVATE or GROUP
    private String name; // optional for group
    private List<Long> memberIds; // required: 1-1 -> 1 other user, group -> members
}
