package com.socialhazard.server.service;

import java.util.Set;

public interface GameIdGenerator {

    String newRoomCode(Set<String> existingCodes);

    String newPlayerId();

    String newPlayerToken();

    String newMatchId();

    String newRoundId();

    String newSubmissionId();
}
