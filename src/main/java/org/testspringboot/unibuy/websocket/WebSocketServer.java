package org.testspringboot.unibuy.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/{role}/{userId}")
public class WebSocketServer {

    // Store concurrent sessions: key = role + "_" + userId
    private static ConcurrentHashMap<String, Session> webSocketMap = new ConcurrentHashMap<>();
    private Session session;
    private String userId;
    private Integer role;
    
    // For JSON serialization
    private static ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("role") Integer role, @PathParam("userId") String userId) {
        this.session = session;
        this.role = role;
        this.userId = userId;
        String key = role + "_" + userId;
        
        if (webSocketMap.containsKey(key)) {
            webSocketMap.remove(key);
            webSocketMap.put(key, session);
        } else {
            webSocketMap.put(key, session);
        }
        log.info("User connected: {}, Online count: {}", key, webSocketMap.size());
    }

    @OnClose
    public void onClose() {
        String key = role + "_" + userId;
        if (webSocketMap.containsKey(key)) {
            webSocketMap.remove(key);
        }
        log.info("User disconnected: {}, Online count: {}", key, webSocketMap.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("Received message from user {}_{}: {}", role, userId, message);
        // Currently we use HTTP for sending messages, WebSocket is mainly for pushing.
        // But we can implement heartbeat here if needed.
        if ("ping".equalsIgnoreCase(message)) {
            try {
                session.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error for user {}_{}: {}", role, userId, error.getMessage());
    }

    /**
     * Send message to specific user
     */
    public static void sendInfo(Object messageObj, Integer role, Long userId) {
        String key = role + "_" + userId;
        try {
            Session session = webSocketMap.get(key);
            if (session != null && session.isOpen()) {
                String jsonMsg = objectMapper.writeValueAsString(messageObj);
                session.getBasicRemote().sendText(jsonMsg);
                log.info("Push message to {}: success", key);
            } else {
                log.info("Push message to {}: user offline", key);
            }
        } catch (Exception e) {
            log.error("Push message to {} failed: {}", key, e.getMessage());
        }
    }
}
