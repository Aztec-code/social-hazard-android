package com.socialhazard.server.model.api.client;

import com.fasterxml.jackson.annotation.JsonAlias;

public record UpdatePlayerNameRequest(
        String roomCode,
        String playerId,
        String playerToken,
        @JsonAlias("playerName") String displayName,
        String avatarId
) {
}
