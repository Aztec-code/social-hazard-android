package com.socialhazard.server.model.api.server;

import java.time.Instant;

public record HealthResponse(
        String status,
        long rooms,
        long activeSessions,
        long connectedPlayers,
        Instant serverTime
) {
}
