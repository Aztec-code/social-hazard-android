package com.socialhazard.server.model.api.client;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CreateRoomRequest(
        @JsonAlias("playerName") String displayName,
        String avatarId,
        Integer targetScore
) {
}
