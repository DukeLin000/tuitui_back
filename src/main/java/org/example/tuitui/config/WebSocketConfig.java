package org.example.tuitui.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Flutter 端連線的端點：ws://localhost:8080/ws-chat
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // 允許跨域
                .withSockJS(); // 備用方案
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 訂閱路徑前綴 (Client 訂閱 /topic/xxx)
        registry.enableSimpleBroker("/topic");
        // 發送路徑前綴 (Client 發送 /app/xxx)
        registry.setApplicationDestinationPrefixes("/app");
    }
}