package com.easyliveline.streamingbackend.websocket;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class SocketConnectionHandler implements WebSocketHandler {

    private final WebSocketSessionManager webSocketSessionManager;
    private final ConcurrentHashMap<String, Long> lastPongTimestamps = new ConcurrentHashMap<>();
    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    public SocketConnectionHandler(WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
        startPingScheduler();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("username");  // Retrieve user ID
        WebSocketSession existingSession = webSocketSessionManager.getSession(userId);
        if (existingSession != null && existingSession.isOpen()) {
            existingSession.sendMessage(new TextMessage("Logout"));
            existingSession.close(); // Gracefully close the session
        }
        webSocketSessionManager.addSession(userId, session);
        lastPongTimestamps.put(userId, System.currentTimeMillis());
        System.out.println("New connection established: " + session.getId());
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session,@NonNull WebSocketMessage<?> message) throws Exception {
        if (message instanceof PongMessage) {
            handlePongMessage(session);
        }else if (message instanceof TextMessage) {
            System.out.println("Received message: " + message.getPayload());
            session.sendMessage(new TextMessage("Message received"));
        }else {
            System.out.println("Unhandled message type: " + message.getClass().getName());
        }
    }

    // Handle the PongMessage to update the client's last active time
    private void handlePongMessage(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("username");
        lastPongTimestamps.put(userId, System.currentTimeMillis()); // Update last pong timestamp
        System.out.println("Pong received from user: " + userId);
    }


    @Override
    public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error for session: " + session.getId());
        exception.printStackTrace();
        session.close(CloseStatus.SERVER_ERROR); // Close the session
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
        String username = (String) session.getAttributes().get("username");
        webSocketSessionManager.removeSession(username);  // Remove session from manager
        lastPongTimestamps.remove(username); // Remove timestamp for closed connection
        System.out.println("Connection closed: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendMessageToAllUsers(String message) {
        webSocketSessionManager.getAllSessions().forEach((userId, session) -> {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendMessageToUser(String username, String message) {
        WebSocketSession session = webSocketSessionManager.getSession(username);
        if (session != null) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startPingScheduler() {
        pingScheduler.scheduleAtFixedRate(() -> {
//            System.out.println("Active Sessions: " + webSocketSessionManager.getAllSessions().size());
            webSocketSessionManager.getAllSessions().forEach((userId, session) -> {
                if (session.isOpen()) {
                    System.out.println("Pinging session: " + session.getId());
                    try {
                        session.sendMessage(new PingMessage(ByteBuffer.wrap("Ping".getBytes())));
//                        session.sendMessage(new TextMessage("Ping"));
                        long lastPong = lastPongTimestamps.getOrDefault(userId, 0L);
                        if (System.currentTimeMillis() - lastPong > 30000) {
                            System.out.println("User inactive: " + userId);

                            session.close();
                            webSocketSessionManager.removeSession(userId);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }, 0, 15, TimeUnit.SECONDS); // Ping every 15 seconds
    }

    // Graceful shutdown logic
    @PreDestroy
    public void cleanup() {
        System.out.println("Cleaning up resources...");
        // Stop the ping scheduler
        pingScheduler.shutdown();
        try {
            if (!pingScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                pingScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            pingScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Close all WebSocket sessions
        webSocketSessionManager.getAllSessions().forEach((userId, session) -> {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Cleanup complete.");
    }
}
