package com.easyliveline.streamingbackend.configs;

import com.easyliveline.streamingbackend.websocket.SocketConnectionHandler;
import com.easyliveline.streamingbackend.websocket.TokenHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SocketConnectionHandler socketConnectionHandler;
    private final TokenHandshakeInterceptor tokenHandshakeInterceptor;

    @Autowired
    public WebSocketConfig(SocketConnectionHandler socketConnectionHandler,
                           TokenHandshakeInterceptor tokenHandshakeInterceptor) {
        this.socketConnectionHandler = socketConnectionHandler;
        this.tokenHandshakeInterceptor = tokenHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("Registering WebSocket Handlers");
        registry.addHandler(socketConnectionHandler, "/ws")
                .setAllowedOrigins("*")  // Allow all origins; restrict in production for security.
                .addInterceptors(tokenHandshakeInterceptor);
    }
}
