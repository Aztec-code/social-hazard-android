package com.socialhazard.server.model.api.client;

public record StartMatchRequest(String roomCode, String playerId, String playerToken) {
}
