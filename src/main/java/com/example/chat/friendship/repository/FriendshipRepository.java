package com.example.chat.friendship.repository;

import com.example.chat.friendship.model.Friendship;
import com.example.chat.friendship.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Check if a relationship already exists (in either direction)
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requesterId = :user1 AND f.addresseeId = :user2) OR " +
            "(f.requesterId = :user2 AND f.addresseeId = :user1)")
    Optional<Friendship> findRelationship(@Param("user1") Long user1, @Param("user2") Long user2);

    // Find all accepted friendships for a specific user
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllFriends(@Param("userId") Long userId);

    // Find pending requests received BY the user (User is the Addressee)
    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, FriendshipStatus status);
}