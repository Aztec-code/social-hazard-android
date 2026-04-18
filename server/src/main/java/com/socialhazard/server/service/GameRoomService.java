package com.socialhazard.server.service;

import com.socialhazard.server.config.GameServerProperties;
import com.socialhazard.server.exception.GameException;
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
import com.socialhazard.server.model.api.server.HealthResponse;
import com.socialhazard.server.model.api.server.PongPayload;
import com.socialhazard.server.model.api.server.RoomClosedPayload;
import com.socialhazard.server.model.api.server.RoomConnectionPayload;
import com.socialhazard.server.model.api.server.RoomLeftPayload;
import com.socialhazard.server.model.api.server.TargetedMessage;
import com.socialhazard.server.model.domain.GameRoom;
import com.socialhazard.server.model.domain.RoomPhase;
import com.socialhazard.server.model.domain.RoomPlayer;
import com.socialhazard.server.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GameRoomService {

    private static final Set<String> ALLOWED_AVATAR_IDS = Set.of(
            "alivepool",
            "boo_bear",
            "drooling_bird",
            "machine_gun_sponge",
            "sonic",
            "why"
    );

    private final RoomRepository roomRepository;
    private final GameIdGenerator idGenerator;
    private final MatchEngine matchEngine;
    private final GameMessageFactory messageFactory;
    private final RoomSessionRegistry sessionRegistry;
    private final Clock clock;
    private final GameServerProperties properties;

    public GameRoomService(
            RoomRepository roomRepository,
            GameIdGenerator idGenerator,
            MatchEngine matchEngine,
            GameMessageFactory messageFactory,
            RoomSessionRegistry sessionRegistry,
            Clock clock,
            GameServerProperties properties
    ) {
        this.roomRepository = roomRepository;
        this.idGenerator = idGenerator;
        this.matchEngine = matchEngine;
        this.messageFactory = messageFactory;
        this.sessionRegistry = sessionRegistry;
        this.clock = clock;
        this.properties = properties;
    }

    public synchronized List<TargetedMessage> createRoom(String sessionId, String requestId, CreateRoomRequest request) {
        assertSessionAvailable(sessionId);
        Instant now = now();
        String displayName = validateDisplayName(request.displayName());
        String avatarId = validateAvatarId(request.avatarId());
        int targetScore = validateTargetScore(request.targetScore());
        String roomCode = idGenerator.newRoomCode(existingRoomCodes());
        String playerId = idGenerator.newPlayerId();
        String playerToken = idGenerator.newPlayerToken();

        GameRoom room = new GameRoom(roomCode, playerId, targetScore, now);
        RoomPlayer host = new RoomPlayer(playerId, displayName, avatarId, playerToken, sessionId, now);
        room.addPlayer(host, now);
        roomRepository.save(room);
        sessionRegistry.bind(sessionId, roomCode, playerId);

        return List.of(messageFactory.message(
                sessionId,
                "RoomCreated",
                requestId,
                new RoomConnectionPayload(roomCode, playerId, playerToken, true, messageFactory.snapshot(room, host)),
                now
        ));
    }

    public synchronized List<TargetedMessage> joinRoom(String sessionId, String requestId, JoinRoomRequest request) {
        assertSessionAvailable(sessionId);
        Instant now = now();
        String roomCode = normalizeRoomCode(request.roomCode());
        String displayName = validateDisplayName(request.displayName());
        String avatarId = validateAvatarId(request.avatarId());
        GameRoom room = requireRoom(roomCode);

        if (room.getPhase() != RoomPhase.LOBBY) {
            throw new GameException("ROOM_ALREADY_STARTED", "The room has already started and cannot accept new players.");
        }
        if (room.getPlayerCount() >= properties.getMaxPlayers()) {
            throw new GameException("ROOM_FULL", "The room is full.");
        }

        String playerId = idGenerator.newPlayerId();
        String playerToken = idGenerator.newPlayerToken();
        RoomPlayer player = new RoomPlayer(playerId, displayName, avatarId, playerToken, sessionId, now);
        room.addPlayer(player, now);
        roomRepository.save(room);
        sessionRegistry.bind(sessionId, roomCode, playerId);

        List<TargetedMessage> deliveries = new ArrayList<>();
        deliveries.add(messageFactory.message(
                sessionId,
                "RoomJoined",
                requestId,
                new RoomConnectionPayload(roomCode, playerId, playerToken, false, messageFactory.snapshot(room, player)),
                now
        ));
        deliveries.addAll(broadcastRoomState(room, now, sessionId));
        return deliveries;
    }

    public synchronized List<TargetedMessage> reconnect(String sessionId, String requestId, ReconnectRequest request) {
        assertSessionAvailable(sessionId);
        Instant now = now();
        String roomCode = normalizeRoomCode(request.roomCode());
        GameRoom room = requireRoom(roomCode);
        RoomPlayer player = requireReconnectPlayer(room, request.playerId(), request.playerToken());

        List<TargetedMessage> deliveries = new ArrayList<>();
        String priorSessionId = player.getSessionId();
        if (priorSessionId != null && !priorSessionId.equals(sessionId)) {
            sessionRegistry.unbind(priorSessionId);
            deliveries.add(messageFactory.closingMessage(
                    priorSessionId,
                    "SessionReplaced",
                    null,
                    new RoomClosedPayload(roomCode, "Reconnected from another device."),
                    now
            ));
        }

        player.attachToSession(sessionId, now);
        room.touch(now);
        roomRepository.save(room);
        sessionRegistry.bind(sessionId, roomCode, player.getPlayerId());

        deliveries.add(messageFactory.message(
                sessionId,
                "ReconnectedState",
                requestId,
                new RoomConnectionPayload(roomCode, player.getPlayerId(), player.getPlayerToken(), room.isHost(player.getPlayerId()), messageFactory.snapshot(room, player)),
                now
        ));
        deliveries.addAll(broadcastRoomState(room, now, sessionId));
        return deliveries;
    }

    public synchronized List<TargetedMessage> leaveRoom(String sessionId, String requestId, LeaveRoomRequest request) {
        Instant now = now();
        AuthorizedPlayer authorizedPlayer = authorize(sessionId, request.roomCode(), request.playerId(), request.playerToken());
        GameRoom room = authorizedPlayer.room();
        RoomPlayer player = authorizedPlayer.player();

        room.removePlayer(player.getPlayerId(), now);
        sessionRegistry.unbind(sessionId);

        List<TargetedMessage> deliveries = new ArrayList<>();
        deliveries.add(messageFactory.message(
                sessionId,
                "RoomLeft",
                requestId,
                new RoomLeftPayload(room.getRoomCode(), player.getPlayerId()),
                now
        ));

        if (room.getPlayerCount() == 0) {
            roomRepository.delete(room.getRoomCode());
            return deliveries;
        }

        if (room.getPhase() != RoomPhase.LOBBY) {
            matchEngine.cancelMatch(room, now);
        }
        roomRepository.save(room);
        deliveries.addAll(broadcastRoomState(room, now, null));
        return deliveries;
    }

    public synchronized List<TargetedMessage> setReadyState(String sessionId, String requestId, ReadyStateRequest request) {
        Instant now = now();
        AuthorizedPlayer authorizedPlayer = authorize(sessionId, request.roomCode(), request.playerId(), request.playerToken());
        GameRoom room = authorizedPlayer.room();
        RoomPlayer player = authorizedPlayer.player();

        if (room.getPhase() != RoomPhase.LOBBY) {
            throw new GameException("MATCH_ALREADY_STARTED", "Ready state can only change while the room is in the lobby.");
        }

        player.setReady(request.ready(), now);
        room.touch(now);
        roomRepository.save(room);
        return broadcastRoomState(room, now, null);
    }

    public synchronized List<TargetedMessage> updatePlayerName(String sessionId, String requestId, UpdatePlayerNameRequest request) {
        Instant now = now();
        AuthorizedPlayer authorizedPlayer = authorize(sessionId, request.roomCode(), request.playerId(), request.playerToken());
        GameRoom room = authorizedPlayer.room();
        RoomPlayer player = authorizedPlayer.player();

        player.updateProfile(validateDisplayName(request.displayName()), validateAvatarId(request.avatarId()), now);
        room.touch(now);
        roomRepository.save(room);
        return broadcastRoomState(room, now, null);
    }

    public synchronized List<TargetedMessage> startMatch(String sessionId, String requestId, StartMatchRequest request) {
        Instant now = now();
        AuthorizedPlayer authorizedPlayer = authorize(sessionId, request.roomCode(), request.playerId(), request.playerToken());
        GameRoom room = authorizedPlayer.room();
        RoomPlayer player = authorizedPlayer.player();

        if (!room.isHost(player.getPlayerId())) {
            throw new GameException("HOST_ONLY", "Only the host can start the match.");
        }
        if (!room.canStartMatch(properties.getMinPlayers(), properties.getMaxPlayers())) {
            throw new GameException("ROOM_NOT_READY", "Every player must be connected and ready before the match can start.");
        }

        matchEngine.startMatch(room, now);
        roomRepository.save(room);

        List<TargetedMessage> deliveries = new ArrayList<>();
        deliveries.addAll(broadcastState(room, "GameStarted", requestId, now, null));
        deliveries.addAll(sendHandUpdates(room, now));
        deliveries.addAll(broadcastState(room, "PromptShown", null, now, null));
        return deliveries;
    }

    public synchronized List<TargetedMessage> submitCard(String sessionId, String requestId, SubmitCardRequest request) {
        Instant now = now();
        AuthorizedPlayer authorizedPlayer = authorize(sessionId, request.roomCode(), request.playerId(), request.playerToken());
        GameRoom room = authorizedPlayer.room();
        RoomPlayer player = authorizedPlayer.player();

        boolean phaseAdvanced = matchEngine.submitCard(room, player, request.roundId(), request.cardIds(), now);
        roomRepository.save(room);

        List<TargetedMessage> deliveries = new ArrayList<>();
        deliveries.add(targetedState(player, room, "SubmissionAccepted", requestId, now));
        if (phaseAdvanced) {
            deliveries.addAll(broadcastState(room, "JudgePhaseStarted", null, now, null));
        } else {
            deliveries.addAll(broadcastState(room, "RoomStateUpdated", null, now, player.getSessionId()));
        }
        return deliveries;
    }

    public synchronized List<TargetedMessage> judgePick(String sessionId, String requestId, JudgePickRequest request) {
        Instant now = now();
        AuthorizedPlayer authorizedPlayer = authorize(sessionId, request.roomCode(), request.playerId(), request.playerToken());
        GameRoom room = authorizedPlayer.room();
        RoomPlayer player = authorizedPlayer.player();

        MatchEngine.JudgeDecision decision = matchEngine.resolveJudgePick(room, player, request.roundId(), request.submissionId(), now);
        roomRepository.save(room);

        List<TargetedMessage> deliveries = new ArrayList<>();
        deliveries.addAll(broadcastState(room, "WinnerChosen", requestId, now, null));

        if (decision.gameOver()) {
            deliveries.addAll(broadcastState(room, "GameOver", null, now, null));
            return deliveries;
        }

        matchEngine.advanceRound(room, now);
        roomRepository.save(room);
        deliveries.addAll(sendHandUpdates(room, now));
        deliveries.addAll(broadcastState(room, "RoundAdvanced", null, now, null));
        deliveries.addAll(broadcastState(room, "PromptShown", null, now, null));
        return deliveries;
    }

    public synchronized List<TargetedMessage> requestStateSync(String sessionId, String requestId, RequestStateSyncRequest request) {
        Instant now = now();
        AuthorizedPlayer authorizedPlayer = authorize(sessionId, request.roomCode(), request.playerId(), request.playerToken());
        roomRepository.save(authorizedPlayer.room());
        return List.of(targetedState(authorizedPlayer.player(), authorizedPlayer.room(), "RoomStateUpdated", requestId, now));
    }

    public synchronized List<TargetedMessage> ping(String sessionId, String requestId, PingRequest request) {
        Instant now = now();
        AuthorizedPlayer authorizedPlayer = authorize(sessionId, request.roomCode(), request.playerId(), request.playerToken());
        GameRoom room = authorizedPlayer.room();
        return List.of(messageFactory.message(
                sessionId,
                "Pong",
                requestId,
                new PongPayload(room.getRoomCode(), room.getStateVersion(), now),
                now
        ));
    }

    public synchronized List<TargetedMessage> handleDisconnect(String sessionId) {
        var binding = sessionRegistry.unbind(sessionId);
        if (binding == null) {
            return List.of();
        }

        GameRoom room = roomRepository.findByCode(binding.roomCode()).orElse(null);
        if (room == null) {
            return List.of();
        }

        RoomPlayer player = room.findPlayer(binding.playerId()).orElse(null);
        if (player == null) {
            return List.of();
        }

        Instant now = now();
        player.markDisconnected(now);
        room.touch(now);
        roomRepository.save(room);

        if (!room.hasConnectedPlayers()) {
            return List.of();
        }
        return broadcastRoomState(room, now, null);
    }

    public synchronized int cleanupStaleRooms() {
        Instant cutoff = now().minus(properties.getStaleRoomThreshold());
        int removedCount = 0;
        Collection<GameRoom> rooms = new ArrayList<>(roomRepository.findAll());
        for (GameRoom room : rooms) {
            if (room.hasConnectedPlayers()) {
                continue;
            }
            if (room.getUpdatedAt().isAfter(cutoff)) {
                continue;
            }
            room.getPlayers().stream()
                    .map(RoomPlayer::getSessionId)
                    .filter(sessionId -> sessionId != null && !sessionId.isBlank())
                    .forEach(sessionRegistry::unbindIfPresent);
            roomRepository.delete(room.getRoomCode());
            removedCount++;
        }
        return removedCount;
    }

    public synchronized HealthResponse getHealth() {
        long connectedPlayers = roomRepository.findAll().stream()
                .mapToLong(GameRoom::getConnectedPlayerCount)
                .sum();
        return new HealthResponse(
                "UP",
                roomRepository.count(),
                sessionRegistry.count(),
                connectedPlayers,
                now()
        );
    }

    private List<TargetedMessage> broadcastRoomState(GameRoom room, Instant now, String excludedSessionId) {
        return broadcastState(room, "RoomStateUpdated", null, now, excludedSessionId);
    }

    private List<TargetedMessage> sendHandUpdates(GameRoom room, Instant now) {
        List<TargetedMessage> deliveries = new ArrayList<>();
        for (RoomPlayer roomPlayer : room.getPlayers()) {
            if (!roomPlayer.isConnected()) {
                continue;
            }
            deliveries.add(targetedState(roomPlayer, room, "HandUpdated", null, now));
        }
        return deliveries;
    }

    private List<TargetedMessage> broadcastState(
            GameRoom room,
            String type,
            String requestId,
            Instant now,
            String excludedSessionId
    ) {
        List<TargetedMessage> deliveries = new ArrayList<>();
        for (RoomPlayer roomPlayer : room.getPlayers()) {
            if (!roomPlayer.isConnected()) {
                continue;
            }
            if (excludedSessionId != null && excludedSessionId.equals(roomPlayer.getSessionId())) {
                continue;
            }
            deliveries.add(targetedState(roomPlayer, room, type, requestId, now));
        }
        return deliveries;
    }

    private TargetedMessage targetedState(RoomPlayer roomPlayer, GameRoom room, String type, String requestId, Instant now) {
        return messageFactory.message(
                roomPlayer.getSessionId(),
                type,
                requestId,
                messageFactory.snapshot(room, roomPlayer),
                now
        );
    }

    private AuthorizedPlayer authorize(String sessionId, String roomCodeRaw, String playerId, String playerToken) {
        var binding = sessionRegistry.find(sessionId);
        if (binding == null) {
            throw new GameException("SESSION_NOT_ATTACHED", "This socket is not attached to a room.");
        }

        String roomCode = normalizeRoomCode(roomCodeRaw);
        if (!binding.roomCode().equals(roomCode) || !binding.playerId().equals(playerId)) {
            throw new GameException("UNAUTHORIZED", "The socket is not authorized for that player.");
        }

        GameRoom room = requireRoom(roomCode);
        RoomPlayer player = requirePlayer(room, playerId);
        if (!player.getPlayerToken().equals(playerToken)) {
            throw new GameException("UNAUTHORIZED", "The player token is invalid.");
        }
        return new AuthorizedPlayer(room, player);
    }

    private void assertSessionAvailable(String sessionId) {
        if (sessionRegistry.isBound(sessionId)) {
            throw new GameException("SESSION_BUSY", "This socket is already attached to a room. Leave before starting another flow.");
        }
    }

    private GameRoom requireRoom(String roomCode) {
        return roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new GameException("ROOM_NOT_FOUND", "The room code was not found."));
    }

    private RoomPlayer requirePlayer(GameRoom room, String playerId) {
        return room.findPlayer(playerId)
                .orElseThrow(() -> new GameException("PLAYER_NOT_FOUND", "The player could not be found in that room."));
    }

    private RoomPlayer requireReconnectPlayer(GameRoom room, String playerId, String playerToken) {
        if (playerToken == null || playerToken.isBlank()) {
            throw new GameException("UNAUTHORIZED", "A player token is required to reconnect.");
        }
        if (playerId != null && !playerId.isBlank()) {
            RoomPlayer player = requirePlayer(room, playerId);
            if (!player.getPlayerToken().equals(playerToken)) {
                throw new GameException("UNAUTHORIZED", "The player token is invalid.");
            }
            return player;
        }
        return room.getPlayers().stream()
                .filter(candidate -> playerToken.equals(candidate.getPlayerToken()))
                .findFirst()
                .orElseThrow(() -> new GameException("PLAYER_NOT_FOUND", "The player token does not match any player in the room."));
    }

    private String normalizeRoomCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new GameException("INVALID_REQUEST", "Room code is required.");
        }
        return roomCode.trim().toUpperCase();
    }

    private String validateDisplayName(String displayName) {
        if (displayName == null) {
            throw new GameException("INVALID_REQUEST", "Display name is required.");
        }
        String normalized = displayName.trim();
        if (normalized.length() < 2 || normalized.length() > 24) {
            throw new GameException("INVALID_REQUEST", "Display name must be between 2 and 24 characters.");
        }
        return normalized;
    }

    private int validateTargetScore(Integer requestedTargetScore) {
        int targetScore = requestedTargetScore == null ? properties.getDefaultTargetScore() : requestedTargetScore;
        if (targetScore < properties.getMinTargetScore() || targetScore > properties.getMaxTargetScore()) {
            throw new GameException(
                    "INVALID_REQUEST",
                    "Target score must be between " + properties.getMinTargetScore() + " and " + properties.getMaxTargetScore() + "."
            );
        }
        return targetScore;
    }

    private String validateAvatarId(String avatarId) {
        if (avatarId == null || avatarId.isBlank()) {
            throw new GameException("INVALID_REQUEST", "Avatar selection is required.");
        }
        String normalized = avatarId.trim();
        if (!ALLOWED_AVATAR_IDS.contains(normalized)) {
            throw new GameException("INVALID_REQUEST", "Avatar selection is invalid.");
        }
        return normalized;
    }

    private Set<String> existingRoomCodes() {
        Set<String> codes = new LinkedHashSet<>();
        roomRepository.findAll().forEach(room -> codes.add(room.getRoomCode()));
        return codes;
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private record AuthorizedPlayer(GameRoom room, RoomPlayer player) {
    }
}
