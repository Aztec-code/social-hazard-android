package com.socialhazard.server.model.api.client;

public record JudgePickRequest(
        String roomCode,
        String playerId,
        String playerToken,
        String roundId,
        String submissionId
) {
}
