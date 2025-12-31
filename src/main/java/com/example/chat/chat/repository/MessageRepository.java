package com.example.chat.chat.repository;

import com.example.chat.chat.model.Message;
import com.example.chat.chat.model.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Spring Data JPA uses the Pageable sort order automatically
    List<Message> findByChatIdAndSenderIdNotAndStatusNot(
            Long chatId,
            Long senderId,
            MessageStatus status
    );
    Page<Message> findByChatId(Long chatId, Pageable pageable);
}