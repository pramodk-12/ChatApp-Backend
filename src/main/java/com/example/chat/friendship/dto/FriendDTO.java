package com.example.chat.friendship.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class FriendDTO {
    private Long id;         // The User ID of the friend
    private String username; // The Username of the friend
    private Long friendshipId; // To help with Unfriending later
}