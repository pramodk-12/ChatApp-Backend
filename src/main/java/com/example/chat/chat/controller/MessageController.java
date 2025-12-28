package com.example.chat.chat.controller;

import com.example.chat.chat.dto.MessageDTO;
import com.example.chat.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;
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
        chatService.saveMessage(messageDto, principal ); // This will now have access to the user
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, messageDto);
    }
}