package com.socialhazard.server.model.api.server;

import java.time.Instant;

public record ServerEnvelope(String type, String requestId, Object payload, Instant serverTime) {
}
