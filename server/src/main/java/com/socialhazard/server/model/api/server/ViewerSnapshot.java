package com.socialhazard.server.model.api.server;

import java.util.List;

public record ViewerSnapshot(
        String playerId,
        boolean canStartGame,
        boolean canToggleReady,
        boolean canRename,
        boolean canSubmitCard,
        boolean canJudgePick,
        List<AnswerCardSnapshot> hand
) {
}
