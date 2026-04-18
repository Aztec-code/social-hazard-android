package com.socialhazard.server.model.api.server;

import java.time.Instant;

public record PongPayload(String roomCode, long stateVersion, Instant serverTime) {
}
