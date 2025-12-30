package com.example.chat.chat.controller;

import com.example.chat.chat.dto.*;
import com.example.chat.chat.service.ChatService;
import com.example.chat.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;
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

    @PostMapping("/private/{targetUserId}")
    public ResponseEntity<ChatDTO> startPrivateChat(@PathVariable Long targetUserId) {
        ChatDTO chat = chatService.getOrCreatePrivateChat(targetUserId);
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatDTO> createGroup(@RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(chatService.createGroupChat(request));
    }


    @DeleteMapping("/{chatId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long chatId,
            @PathVariable Long messageId
    ) {
        messageService.deleteMessage(messageId);

        ChatEventDTO event = new ChatEventDTO("DELETE_MESSAGE", messageId);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, event);

        return ResponseEntity.noContent().build();
    }


}
