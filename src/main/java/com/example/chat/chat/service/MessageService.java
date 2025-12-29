package com.example.chat.chat.service;

import com.example.chat.auth.models.User;
import com.example.chat.auth.repositories.UserRepository;
import com.example.chat.chat.dto.MessageDTO;
import com.example.chat.chat.model.Message;
import com.example.chat.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<MessageDTO> getRecentMessages(Long chatId, int limit) {
        // 1. Create a page request for the first page, specific size, sorted by newest first
        Pageable pageable = PageRequest.of(0, limit, Sort.by("timestamp").descending());

        // 2. Fetch from DB
        return messageRepository.findByChatId(chatId, pageable)
                .getContent() // Converts Page<Message> to List<Message>
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MessageDTO convertToDto(Message message) {
        String senderName = userRepository.findById(message.getSenderId())
                .map(User::getUsername)
                .orElse("Unknown User");
        return MessageDTO.builder()
                .id(message.getId())
                .chatId(message.getChatId())
                .senderId(message.getSenderId())
                .senderName(senderName)
                .content(message.getContent())
                .mediaUrl(message.getMediaUrl())
                .status(message.getStatus())
                .isDeleted(message.isDeleted())
                .timestamp(message.getTimestamp())
                .build();
    }
}
