package com.socialhazard.server.model.api.server;

public record RoomConnectionPayload(
        String roomCode,
        String playerId,
        String playerToken,
        boolean host,
        RoomSnapshot state
) {
}
