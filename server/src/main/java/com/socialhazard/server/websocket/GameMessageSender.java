package com.socialhazard.server.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialhazard.server.model.api.server.TargetedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameMessageSender {

    private static final Logger logger = LoggerFactory.getLogger(GameMessageSender.class);

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public GameMessageSender(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void unregister(String sessionId) {
        sessions.remove(sessionId);
    }

    public long activeSessionCount() {
        return sessions.size();
    }

    public void sendAll(List<TargetedMessage> deliveries) {
        for (TargetedMessage delivery : deliveries) {
            send(delivery);
        }
    }

    public void send(TargetedMessage delivery) {
        WebSocketSession session = sessions.get(delivery.sessionId());
        if (session == null || !session.isOpen()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(delivery.message());
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
            if (delivery.closeAfterSend()) {
                session.close(CloseStatus.NORMAL);
            }
        } catch (JsonProcessingException exception) {
            logger.error("Failed to serialize outgoing websocket payload for session {}", delivery.sessionId(), exception);
        } catch (IOException exception) {
            logger.warn("Failed to send websocket payload to session {}", delivery.sessionId(), exception);
            tryClose(session);
        }
    }

    private void tryClose(WebSocketSession session) {
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException exception) {
            logger.debug("Failed to close websocket session {}", session.getId(), exception);
        }
    }
}
