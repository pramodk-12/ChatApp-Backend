package com.example.chat.chat.repository;

import com.example.chat.chat.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 1. Fixed the path and used the mapped 'members' relationship
    @Query("SELECT c FROM Chat c JOIN c.members m WHERE m.userId = :userId " +
            "ORDER BY c.lastMessageTimestamp DESC, c.createdAt DESC")
    List<Chat> findByMemberUserId(@Param("userId") Long userId);

    // 2. FIXED: Changed :user to :userB to match the @Param name
    @Query("""
        SELECT c FROM Chat c
        JOIN c.members m1
        JOIN c.members m2
        WHERE c.type = 'PRIVATE'
        AND m1.userId = :userA
        AND m2.userId = :userB
    """)
    List<Chat> findPrivateChatBetween(@Param("userA") Long userA, @Param("userB") Long userB);
}