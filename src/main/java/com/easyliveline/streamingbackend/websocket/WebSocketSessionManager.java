package com.easyliveline.streamingbackend.websocket;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Add a session
    public void addSession(String userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    // Remove a session
    public void removeSession(String userId) {
        sessions.remove(userId);
    }

    // Get a session by userId
    public WebSocketSession getSession(String userId) {
        return sessions.get(userId);
    }

    // Check if a user session exists
    public boolean hasSession(String userId) {
        return sessions.containsKey(userId);
    }

    // Get all sessions (if needed)
    public ConcurrentHashMap<String, WebSocketSession> getAllSessions() {
        return sessions;
    }
}
