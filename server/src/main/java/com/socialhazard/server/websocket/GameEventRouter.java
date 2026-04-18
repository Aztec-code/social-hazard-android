package com.socialhazard.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialhazard.server.exception.GameException;
import com.socialhazard.server.model.api.client.ClientEnvelope;
import com.socialhazard.server.model.api.client.CreateRoomRequest;
import com.socialhazard.server.model.api.client.JudgePickRequest;
import com.socialhazard.server.model.api.client.JoinRoomRequest;
import com.socialhazard.server.model.api.client.LeaveRoomRequest;
import com.socialhazard.server.model.api.client.PingRequest;
import com.socialhazard.server.model.api.client.ReadyStateRequest;
import com.socialhazard.server.model.api.client.ReconnectRequest;
import com.socialhazard.server.model.api.client.RequestStateSyncRequest;
import com.socialhazard.server.model.api.client.StartMatchRequest;
import com.socialhazard.server.model.api.client.SubmitCardRequest;
import com.socialhazard.server.model.api.client.UpdatePlayerNameRequest;
import com.socialhazard.server.model.api.server.TargetedMessage;
import com.socialhazard.server.service.GameRoomService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class GameEventRouter {

    private final ObjectMapper objectMapper;
    private final GameRoomService gameRoomService;

    public GameEventRouter(ObjectMapper objectMapper, GameRoomService gameRoomService) {
        this.objectMapper = objectMapper;
        this.gameRoomService = gameRoomService;
    }

    public List<TargetedMessage> route(String sessionId, ClientEnvelope envelope) throws IOException {
        // Accept a few legacy aliases so older clients can survive protocol cleanup.
        return switch (canonicalize(envelope.type())) {
            case "createroom" -> gameRoomService.createRoom(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, CreateRoomRequest.class)
            );
            case "joinroom" -> gameRoomService.joinRoom(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, JoinRoomRequest.class)
            );
            case "reconnect", "reconnectroom" -> gameRoomService.reconnect(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, ReconnectRequest.class)
            );
            case "leaveroom" -> gameRoomService.leaveRoom(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, LeaveRoomRequest.class)
            );
            case "toggleready", "readystate" -> gameRoomService.setReadyState(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, ReadyStateRequest.class)
            );
            case "updateplayername" -> gameRoomService.updatePlayerName(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, UpdatePlayerNameRequest.class)
            );
            case "startgame", "startmatch" -> gameRoomService.startMatch(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, StartMatchRequest.class)
            );
            case "submitcard" -> gameRoomService.submitCard(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, SubmitCardRequest.class)
            );
            case "judgepick" -> gameRoomService.judgePick(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, JudgePickRequest.class)
            );
            case "requeststatesync" -> gameRoomService.requestStateSync(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, RequestStateSyncRequest.class)
            );
            case "ping" -> gameRoomService.ping(
                    sessionId,
                    envelope.requestId(),
                    readPayload(envelope, PingRequest.class)
            );
            default -> throw new GameException("UNKNOWN_TYPE", "Unsupported message type: " + envelope.type());
        };
    }

    private <T> T readPayload(ClientEnvelope envelope, Class<T> payloadType) throws IOException {
        return objectMapper.treeToValue(envelope.payload(), payloadType);
    }

    private String canonicalize(String type) {
        if (type == null) {
            return "";
        }
        return type.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT);
    }
}
