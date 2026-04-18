package com.socialhazard.server.model.api.client;

public record ReadyStateRequest(String roomCode, String playerId, String playerToken, boolean ready) {
}
