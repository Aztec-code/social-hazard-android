package com.socialhazard.server.model.api.client;

public record LeaveRoomRequest(String roomCode, String playerId, String playerToken) {
}
