package com.socialhazard.server.model.api.client;

public record PingRequest(
        String roomCode,
        String playerId,
        String playerToken,
        Long lastKnownStateVersion
) {
}
