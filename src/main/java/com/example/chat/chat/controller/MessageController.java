package com.example.chat.chat.controller;

import com.example.chat.chat.dto.MessageDTO;
import com.example.chat.chat.service.ChatService;
import com.example.chat.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Alice sends a message to: /app/chat/{chatId}/send
     * The @Payload is now the DTO to match your Service method.
     */
    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(
            @DestinationVariable Long chatId,
            @Payload MessageDTO messageDto,
            Principal principal // Spring injects the user we 'stamped' in the interceptor
    ) {
        System.out.println("Message received from: " + principal.getName());
        // Or cast it back to our User object if needed
        messageDto.setChatId(chatId);
        MessageDTO savedDto = chatService.saveMessage(messageDto, principal ); // This will now have access to the user
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, savedDto);
    }

    @GetMapping("/api/messages/{chatId}")
    public ResponseEntity<List<MessageDTO>> getChatHistory(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        // Assuming you have a MessageRepository with a findByChatIdOrderByTimestampDesc method
        // We fetch them and then reverse them so they are in chronological order
        List<MessageDTO> history = messageService.getRecentMessages(chatId, limit);
        return ResponseEntity.ok(history);
    }
}