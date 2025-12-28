package com.example.chat.chat.controller;

import com.example.chat.chat.dto.AddMemberDTO;
import com.example.chat.chat.dto.ChatDTO;
import com.example.chat.chat.dto.CreateChatDTO;
import com.example.chat.chat.dto.MessageDTO;
import com.example.chat.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("")
    public ResponseEntity<ChatDTO> createChat(@RequestBody CreateChatDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createChat(dto));
    }

    @GetMapping("")
    public List<ChatDTO> getMyChats() {
        return chatService.getChatsForCurrentUser();
    }

    @GetMapping("/{chatId}/messages")
    public Page<MessageDTO> getMessages(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return chatService.getMessages(chatId, page, size);
    }

    @PostMapping("/{chatId}/members")
    public void addMember(@PathVariable Long chatId, @RequestBody AddMemberDTO dto) {
        chatService.addMember(chatId, dto);
    }

    @DeleteMapping("/{chatId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long chatId, @PathVariable Long userId) {
        chatService.removeMember(chatId, userId);
        return ResponseEntity.noContent().build(); // 204 No Content is standard for deletes
    }
}
