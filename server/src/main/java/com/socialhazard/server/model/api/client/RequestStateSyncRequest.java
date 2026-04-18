package com.socialhazard.server.model.api.client;

public record RequestStateSyncRequest(
        String roomCode,
        String playerId,
        String playerToken,
        Long lastKnownStateVersion,
        String reason
) {
}
