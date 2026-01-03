package com.example.chat.chat.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateGroupRequest {
    private String name;
    private List<Long> memberIds; // The IDs of friends to add
}