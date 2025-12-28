package com.example.chat.chat.repository;

import com.example.chat.chat.model.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    List<ChatMember> findByChatId(Long chatId);
    List<ChatMember> findByUserId(Long userId);
    Optional<ChatMember> findByChatIdAndUserId(Long chatId, Long userId);
    void deleteByChatIdAndUserId(Long chatId, Long userId);
}

