package com.socialhazard.server.model.api.server;

import java.util.List;

public record RoundSnapshot(
        String matchId,
        String roundId,
        int roundNumber,
        String judgePlayerId,
        PromptSnapshot prompt,
        int requiredSubmissions,
        int receivedSubmissions,
        List<SubmissionSnapshot> submissions,
        String winningSubmissionId,
        String winnerPlayerId
) {
}
