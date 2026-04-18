package com.socialhazard.server.model.api.server;

import java.util.List;

public record SubmissionSnapshot(
        String submissionId,
        List<String> cardIds,
        String text,
        String submittedByPlayerId,
        String submittedByName,
        boolean winner,
        String origin
) {
}
