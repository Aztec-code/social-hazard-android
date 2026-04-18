package com.socialhazard.server.model.api.client;

import java.util.List;

public record SubmitCardRequest(
        String roomCode,
        String playerId,
        String playerToken,
        String roundId,
        List<String> cardIds
) {
}
