package com.socialhazard.server.model.api.server;

import com.socialhazard.server.model.domain.GameMode;
import com.socialhazard.server.model.domain.RoomPhase;

import java.time.Instant;
import java.util.List;

public record RoomSnapshot(
        String roomCode,
        RoomPhase phase,
        GameMode mode,
        int targetScore,
        long stateVersion,
        String hostPlayerId,
        boolean canStart,
        int playerCount,
        int connectedPlayers,
        List<PlayerSnapshot> players,
        ViewerSnapshot viewer,
        RoundSnapshot round,
        List<ScoreSnapshot> scoreboard,
        Instant createdAt,
        Instant updatedAt
) {
}
