package com.example.chat.websocket.listener;

import com.example.chat.auth.models.User;
import com.example.chat.presence.dto.UserPresenceDTO;
import com.example.chat.presence.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketPresenceListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            User user = (User) auth.getPrincipal();
            presenceService.markOnline(user.getId());
            broadcastPresence();
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth) {
            User user = (User) auth.getPrincipal();
            presenceService.markOffline(user.getId());
            broadcastPresence();
        }
    }

    @EventListener
    public void handleSubscription(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // Check if the user is subscribing to the presence topic
        if ("/topic/presence".equals(accessor.getDestination())) {
            System.out.println("📬 New subscriber to presence. Sending current list...");
            broadcastPresence();
        }
    }

    private void broadcastPresence() {
        List<UserPresenceDTO> statusList = presenceService.getAllPresenceStatus();
        messagingTemplate.convertAndSend("/topic/presence", statusList);
    }
}