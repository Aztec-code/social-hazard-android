package com.socialhazard.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialhazard.server.exception.GameException;
import com.socialhazard.server.model.api.client.ClientEnvelope;
import com.socialhazard.server.model.api.server.TargetedMessage;
import com.socialhazard.server.service.GameMessageFactory;
import com.socialhazard.server.service.GameRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final GameEventRouter eventRouter;
    private final GameRoomService gameRoomService;
    private final GameMessageFactory messageFactory;
    private final GameMessageSender messageSender;

    public GameWebSocketHandler(
            ObjectMapper objectMapper,
            GameEventRouter eventRouter,
            GameRoomService gameRoomService,
            GameMessageFactory messageFactory,
            GameMessageSender messageSender
    ) {
        this.objectMapper = objectMapper;
        this.eventRouter = eventRouter;
        this.gameRoomService = gameRoomService;
        this.messageFactory = messageFactory;
        this.messageSender = messageSender;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        messageSender.register(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        ClientEnvelope envelope = null;
        try {
            envelope = objectMapper.readValue(message.getPayload(), ClientEnvelope.class);
            List<TargetedMessage> deliveries = eventRouter.route(session.getId(), envelope);
            messageSender.sendAll(deliveries);
        } catch (GameException exception) {
            sendError(session.getId(), envelope == null ? null : envelope.requestId(), exception.getCode(), exception.getMessage());
        } catch (IOException exception) {
            sendError(session.getId(), envelope == null ? null : envelope.requestId(), "INVALID_JSON", "Incoming websocket payload was not valid JSON.");
        } catch (Exception exception) {
            logger.error("Unexpected websocket error for session {}", session.getId(), exception);
            sendError(session.getId(), envelope == null ? null : envelope.requestId(), "INTERNAL_ERROR", "Unexpected server error.");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.warn("Transport error for websocket session {}", session.getId(), exception);
        messageSender.unregister(session.getId());
        messageSender.sendAll(gameRoomService.handleDisconnect(session.getId()));
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        messageSender.unregister(session.getId());
        messageSender.sendAll(gameRoomService.handleDisconnect(session.getId()));
    }

    private void sendError(String sessionId, String requestId, String code, String message) {
        Instant now = Instant.now();
        messageSender.send(new TargetedMessage(
                sessionId,
                messageFactory.error(requestId, code, message, now),
                false
        ));
    }
}
