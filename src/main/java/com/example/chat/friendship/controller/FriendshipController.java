package com.example.chat.friendship.controller;

import com.example.chat.friendship.dto.FriendDTO;
import com.example.chat.friendship.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    // Send a request
    @PostMapping("/request/{targetUserId}")
    public ResponseEntity<Void> sendFriendRequest(@PathVariable Long targetUserId) {
        friendshipService.sendRequest(targetUserId);
        return ResponseEntity.ok().build();
    }

    // Accept a request (using the Friendship ID, not User ID)
    @PutMapping("/accept/{requestId}")
    public ResponseEntity<Void> acceptRequest(@PathVariable Long requestId) {
        friendshipService.acceptRequest(requestId);
        return ResponseEntity.ok().build();
    }

    // Get my friend list
    @GetMapping
    public ResponseEntity<List<FriendDTO>> getMyFriends() {
        return ResponseEntity.ok(friendshipService.getMyFriends());
    }

    // Get pending requests
    @GetMapping("/requests")
    public ResponseEntity<List<FriendDTO>> getPendingRequests() {
        return ResponseEntity.ok(friendshipService.getPendingRequests());
    }
}