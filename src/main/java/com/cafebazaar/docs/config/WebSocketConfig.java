package com.cafebazaar.docs.config;

import com.cafebazaar.docs.websocket.DocumentEditSessionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DocumentEditSessionHandler documentEditSessionHandler;

    @Autowired
    public WebSocketConfig(DocumentEditSessionHandler documentEditSessionHandler) {
        this.documentEditSessionHandler = documentEditSessionHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(documentEditSessionHandler, "/ws-doc-edit/{docId}").setAllowedOrigins("*");
    }

}
