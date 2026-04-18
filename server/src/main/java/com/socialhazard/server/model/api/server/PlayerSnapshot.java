package com.socialhazard.server.model.api.server;

public record PlayerSnapshot(
        String playerId,
        String displayName,
        String avatarId,
        int seatIndex,
        boolean host,
        boolean ready,
        boolean connected,
        int score,
        boolean judge,
        boolean submitted
) {
}
