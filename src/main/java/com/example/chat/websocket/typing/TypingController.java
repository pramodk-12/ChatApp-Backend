package com.example.chat.websocket.typing;

import com.example.chat.auth.models.User;
import com.example.chat.presence.service.PresenceService;
import com.example.chat.websocket.dto.TypingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TypingController {

    private final TypingService typingService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{chatId}/typing")
    public void typing(
            @DestinationVariable Long chatId,
            @Payload TypingRequest request, // Add a small DTO to handle true/false
            Principal principal
    ) {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        if (request.getIsTyping()) {
            typingService.markTyping(chatId, user.getId());
        }

        // Create a small Map or DTO to match frontend JSON expectation
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", user.getUsername());
        payload.put("isTyping", request.getIsTyping());

        // Fix the path: Add the 's' to match /topic/chats/
        // Cast the payload to Object to resolve the ambiguity
        messagingTemplate.convertAndSend(
                (String) "/topic/chats/" + chatId + "/typing",
                (Object) payload
        );
    }

    @MessageMapping("/heartbeat")
    public void handleHeartbeat(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            User user = (User) auth.getPrincipal();

            // This resets the 60-second timer in Redis
            presenceService.markOnline(user.getId());

            // Optional: Broadcast if you want 'Last Seen' to be updated in real-time
            // broadcastPresence();
        }
    }
}
