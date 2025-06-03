package com.easyliveline.streamingbackend.websocket;

import com.easyliveline.streamingbackend.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class TokenHandshakeInterceptor implements HandshakeInterceptor {

//    private final WebSocketSessionManager webSocketSessionManager;
//
//    @Autowired
//    public TokenHandshakeInterceptor(WebSocketSessionManager webSocketSessionManager) {
//        this.webSocketSessionManager = webSocketSessionManager;
//    }
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, @NonNull ServerHttpResponse response,@NonNull WebSocketHandler wsHandler,@NonNull Map<String, Object> attributes) throws Exception {
        // Extract token from the query parameters
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1];

            // Validate token
            if (JwtUtil.validateToken(token)) {
                attributes.put("token", token); // Store the valid token in the WebSocket connection attributes
                String userName = JwtUtil.getUserId(token); // Extract userName from the token
                attributes.put("username", userName);
//                WebSocketSession session = (WebSocketSession) wsHandler;
//                webSocketSessionManager.addSession(userName, session);
                return true; // Proceed with the WebSocket handshake
            } else {
                // Invalid token, send a 401 Unauthorized response and close the connection
                response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401 Unauthorized status
                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                response.getBody().write("Invalid or expired token".getBytes()); // Optionally send an error message
                return false; // Cancel the handshake
            }
        } else {
            // No token found in the query string, send a 400 Bad Request response
            response.setStatusCode(HttpStatus.BAD_REQUEST); // 400 Bad Request status
            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
            response.getBody().write("Token is required".getBytes()); // Optionally send an error message
            return false; // Cancel the handshake
        }
    }


    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,@NonNull ServerHttpResponse response,@NonNull WebSocketHandler wsHandler, Exception exception) {
        // Optional: You can handle logic after the handshake here if needed
        System.out.println("Handshake completed.");
    }
}