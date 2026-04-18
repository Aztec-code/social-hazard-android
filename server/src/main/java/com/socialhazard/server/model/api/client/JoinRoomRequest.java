package com.socialhazard.server.model.api.client;

import com.fasterxml.jackson.annotation.JsonAlias;

public record JoinRoomRequest(
        String roomCode,
        @JsonAlias("playerName") String displayName,
        String avatarId
) {
}
