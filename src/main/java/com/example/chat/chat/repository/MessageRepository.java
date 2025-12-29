package com.example.chat.chat.repository;

import com.example.chat.chat.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Spring Data JPA uses the Pageable sort order automatically
    Page<Message> findByChatId(Long chatId, Pageable pageable);
}