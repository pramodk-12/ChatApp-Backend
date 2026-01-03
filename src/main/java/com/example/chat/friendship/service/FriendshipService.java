package com.example.chat.friendship.service;

import com.example.chat.auth.models.User;
import com.example.chat.auth.repositories.UserRepository;
import com.example.chat.friendship.dto.FriendDTO;
import com.example.chat.friendship.model.Friendship;
import com.example.chat.friendship.model.FriendshipStatus;
import com.example.chat.friendship.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepo;
    private final UserRepository userRepo;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    public void sendRequest(Long targetUserId) {
        User me = getCurrentUser();
        if (me.getId().equals(targetUserId)) {
            throw new RuntimeException("You cannot add yourself as a friend");
        }

        // Check if connection exists
        if (friendshipRepo.findRelationship(me.getId(), targetUserId).isPresent()) {
            throw new RuntimeException("Friendship or request already exists");
        }

        Friendship friendship = Friendship.builder()
                .requesterId(me.getId())
                .addresseeId(targetUserId)
                .status(FriendshipStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        friendshipRepo.save(friendship);
    }

    @Transactional
    public void acceptRequest(Long requestId) {
        Friendship request = friendshipRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User me = getCurrentUser();
        if (!request.getAddresseeId().equals(me.getId())) {
            throw new RuntimeException("This request was not sent to you");
        }

        request.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepo.save(request);
    }

    public List<FriendDTO> getMyFriends() {
        User me = getCurrentUser();
        List<Friendship> friendships = friendshipRepo.findAllFriends(me.getId());

        return friendships.stream().map(f -> {
            // Determine which ID belongs to the "Friend" (not me)
            Long friendId = f.getRequesterId().equals(me.getId()) ? f.getAddresseeId() : f.getRequesterId();

            // In a real app, optimize this by fetching all Users in one query (IN clause)
            User friend = userRepo.findById(friendId).orElseThrow();

            return new FriendDTO(friend.getId(), friend.getUsername(), f.getId());
        }).collect(Collectors.toList());
    }

    public List<FriendDTO> getPendingRequests() {
        User me = getCurrentUser();
        // Only show requests waiting for MY approval
        return friendshipRepo.findByAddresseeIdAndStatus(me.getId(), FriendshipStatus.PENDING)
                .stream()
                .map(f -> {
                    User requester = userRepo.findById(f.getRequesterId()).orElseThrow();
                    return new FriendDTO(requester.getId(), requester.getUsername(), f.getId());
                })
                .collect(Collectors.toList());
    }

    // Helper to check friendship (for ChatService later)
    public boolean areFriends(Long user1Id, Long user2Id) {
        return friendshipRepo.findRelationship(user1Id, user2Id)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }
}