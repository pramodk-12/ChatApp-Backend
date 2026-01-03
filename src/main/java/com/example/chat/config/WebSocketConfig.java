package com.example.chat.config;

import com.example.chat.websocket.interceptor.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Messages starting with /topic are sent from server to clients (Subscribing)
        config.enableSimpleBroker("/topic");

        // Messages starting with /app are sent from clients to server (Publishing)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the URL the frontend will connect to: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(frontendUrl) // Be more specific in production!
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}