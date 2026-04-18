package com.socialhazard.server.model.api.client;

public record ReconnectRequest(String roomCode, String playerId, String playerToken, Long lastKnownStateVersion) {
}
