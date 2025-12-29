package com.example.chat.chat.controller;

import com.example.chat.chat.dto.*;
import com.example.chat.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("")
    public ResponseEntity<ChatDTO> createChat(@RequestBody CreateChatDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createChat(dto));
    }

    @GetMapping("")
    public List<ChatDTO> getMyChats() {
        return chatService.getChatsForCurrentUser();
    }


    @PostMapping("/{chatId}/members")
    public void addMember(@PathVariable Long chatId, @RequestBody AddMemberDTO dto) {
        chatService.addMember(chatId, dto);
    }

    @DeleteMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long chatId,
            @PathVariable Long messageId
    ) {
        chatService.deleteMessage(messageId);

        ChatEventDTO event = new ChatEventDTO("DELETE_MESSAGE", messageId);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, event);

        return ResponseEntity.noContent().build();
    }
}
