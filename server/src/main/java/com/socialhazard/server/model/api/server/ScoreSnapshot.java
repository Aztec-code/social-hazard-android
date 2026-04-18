package com.socialhazard.server.model.api.server;

public record ScoreSnapshot(String playerId, String displayName, String avatarId, int score, int rank) {
}
