package com.socialhazard.app.repository;

import com.socialhazard.app.model.ConnectionStatus;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.RoomSession;
import com.socialhazard.app.util.AvatarCatalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class DemoScenarioFactory {

    static final String ROOM_CODE = "DEMO";
    static final String LOCAL_PLAYER_ID = "demo_local";
    static final String LOCAL_PLAYER_NAME = "Avery";
    static final String LOCAL_AVATAR_ID = AvatarCatalog.AVATAR_SONIC;
    static final String BOT_ONE_ID = "demo_bot_one";
    static final String BOT_ONE_NAME = "Jules";
    static final String BOT_ONE_AVATAR_ID = AvatarCatalog.AVATAR_BOO_BEAR;
    static final String BOT_TWO_ID = "demo_bot_two";
    static final String BOT_TWO_NAME = "Nina";
    static final String BOT_TWO_AVATAR_ID = AvatarCatalog.AVATAR_DROOLING_BIRD;
    static final String BOT_THREE_ID = "demo_bot_three";
    static final String BOT_THREE_NAME = "Malik";
    static final String BOT_THREE_AVATAR_ID = AvatarCatalog.AVATAR_ALIVEPOOL;

    static final String SUBMISSION_ALPHA = "demo_submission_alpha";
    static final String SUBMISSION_BETA = "demo_submission_beta";
    static final String SUBMISSION_GAMMA = "demo_submission_gamma";

    private static final int TARGET_SCORE = 5;
    private static final RoomSession DEMO_SESSION = new RoomSession(
            ROOM_CODE,
            LOCAL_PLAYER_ID,
            "demo_token",
            LOCAL_PLAYER_NAME,
            LOCAL_AVATAR_ID
    );
    private static final String DEMO_STATUS = "Offline solo demo room.";

    RoomSession demoSession() {
        return DEMO_SESSION;
    }

    GameSessionState lobbyState(boolean localReady) {
        List<GameSessionState.Player> players = List.of(
                player(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 0, true, localReady, true, 0, false, false),
                player(BOT_ONE_ID, BOT_ONE_NAME, BOT_ONE_AVATAR_ID, 1, false, true, true, 0, false, false),
                player(BOT_TWO_ID, BOT_TWO_NAME, BOT_TWO_AVATAR_ID, 2, false, true, true, 0, false, false),
                player(BOT_THREE_ID, BOT_THREE_NAME, BOT_THREE_AVATAR_ID, 3, false, true, true, 0, false, false)
        );

        GameSessionState.RoomState roomState = new GameSessionState.RoomState(
                ROOM_CODE,
                GameSessionState.Phase.LOBBY,
                GameSessionState.Mode.CLASSIC,
                TARGET_SCORE,
                1L,
                LOCAL_PLAYER_ID,
                localReady,
                players.size(),
                players.size(),
                players,
                new GameSessionState.Viewer(
                        LOCAL_PLAYER_ID,
                        localReady,
                        true,
                        false,
                        false,
                        false,
                        List.of()
                ),
                null,
                scoreboard(
                        score(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 0),
                        score(BOT_ONE_ID, BOT_ONE_NAME, BOT_ONE_AVATAR_ID, 0),
                        score(BOT_TWO_ID, BOT_TWO_NAME, BOT_TWO_AVATAR_ID, 0),
                        score(BOT_THREE_ID, BOT_THREE_NAME, BOT_THREE_AVATAR_ID, 0)
                )
        );

        return state(roomState, null, DEMO_STATUS);
    }

    GameSessionState submittingState(boolean localSubmitted) {
        List<GameSessionState.AnswerCard> hand = localSubmitted ? List.of() : List.of(
                answer("demo_card_01", "Silence."),
                answer("demo_card_02", "The illusion of choice in a late-stage capitalist society."),
                answer("demo_card_03", "Many bats."),
                answer("demo_card_04", "Famine."),
                answer("demo_card_05", "Flesh-eating bacteria."),
                answer("demo_card_06", "Flying sex snakes.")
        );

        List<GameSessionState.Submission> visibleSubmissions = localSubmitted
                ? List.of(submission(
                "demo_submission_local",
                "demo_card_03",
                "Many bats.",
                LOCAL_PLAYER_ID,
                LOCAL_PLAYER_NAME,
                false
        ))
                : List.of();

        List<GameSessionState.Player> players = List.of(
                player(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 0, true, true, true, 3, false, localSubmitted),
                player(BOT_ONE_ID, BOT_ONE_NAME, BOT_ONE_AVATAR_ID, 1, false, true, true, 2, true, false),
                player(BOT_TWO_ID, BOT_TWO_NAME, BOT_TWO_AVATAR_ID, 2, false, true, true, 3, false, true),
                player(BOT_THREE_ID, BOT_THREE_NAME, BOT_THREE_AVATAR_ID, 3, false, true, true, 1, false, true)
        );

        GameSessionState.Round roundRoom = new GameSessionState.Round(
                "demo_match",
                "demo_round_03",
                3,
                BOT_ONE_ID,
                new GameSessionState.Prompt("demo_prompt_03", "What is Batman's guilty pleasure?", 1),
                3,
                localSubmitted ? 3 : 2,
                visibleSubmissions,
                null,
                null
        );

        GameSessionState.RoomState roomState = new GameSessionState.RoomState(
                ROOM_CODE,
                GameSessionState.Phase.SUBMITTING,
                GameSessionState.Mode.CLASSIC,
                TARGET_SCORE,
                localSubmitted ? 3L : 2L,
                LOCAL_PLAYER_ID,
                false,
                players.size(),
                players.size(),
                players,
                new GameSessionState.Viewer(
                        LOCAL_PLAYER_ID,
                        false,
                        false,
                        false,
                        !localSubmitted,
                        false,
                        hand
                ),
                roundRoom,
                scoreboard(
                        score(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 3),
                        score(BOT_TWO_ID, BOT_TWO_NAME, BOT_TWO_AVATAR_ID, 3),
                        score(BOT_ONE_ID, BOT_ONE_NAME, BOT_ONE_AVATAR_ID, 2),
                        score(BOT_THREE_ID, BOT_THREE_NAME, BOT_THREE_AVATAR_ID, 1)
                )
        );

        String status = localSubmitted
                ? "Bot players are wrapping up the local demo round."
                : DEMO_STATUS;
        return state(roomState, null, status);
    }

    GameSessionState roundResultState() {
        List<GameSessionState.Player> players = List.of(
                player(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 0, true, true, true, 4, false, false),
                player(BOT_ONE_ID, BOT_ONE_NAME, BOT_ONE_AVATAR_ID, 1, false, true, true, 2, false, false),
                player(BOT_TWO_ID, BOT_TWO_NAME, BOT_TWO_AVATAR_ID, 2, false, true, true, 3, false, false),
                player(BOT_THREE_ID, BOT_THREE_NAME, BOT_THREE_AVATAR_ID, 3, false, true, true, 1, false, false)
        );
        List<GameSessionState.Score> scoreboard = scoreboard(
                score(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 4),
                score(BOT_TWO_ID, BOT_TWO_NAME, BOT_TWO_AVATAR_ID, 3),
                score(BOT_ONE_ID, BOT_ONE_NAME, BOT_ONE_AVATAR_ID, 2),
                score(BOT_THREE_ID, BOT_THREE_NAME, BOT_THREE_AVATAR_ID, 1)
        );

        GameSessionState.RoomState roomState = new GameSessionState.RoomState(
                ROOM_CODE,
                GameSessionState.Phase.ROUND_RESULT,
                GameSessionState.Mode.CLASSIC,
                TARGET_SCORE,
                4L,
                LOCAL_PLAYER_ID,
                false,
                players.size(),
                players.size(),
                players,
                new GameSessionState.Viewer(
                        LOCAL_PLAYER_ID,
                        false,
                        false,
                        false,
                        false,
                        false,
                        List.of()
                ),
                new GameSessionState.Round(
                        "demo_match",
                        "demo_round_03",
                        3,
                        BOT_ONE_ID,
                        new GameSessionState.Prompt("demo_prompt_03", "What is Batman's guilty pleasure?", 1),
                        3,
                        3,
                        List.of(),
                        "demo_submission_local",
                        LOCAL_PLAYER_ID
                ),
                scoreboard
        );

        return state(
                roomState,
                new GameSessionState.RoundReveal(
                        3,
                        LOCAL_PLAYER_NAME,
                        "Many bats.",
                        scoreboard
                ),
                "The demo is ready to jump into judge next."
        );
    }

    GameSessionState judgingState() {
        List<GameSessionState.Player> players = List.of(
                player(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 0, true, true, true, 4, true, false),
                player(BOT_ONE_ID, BOT_ONE_NAME, BOT_ONE_AVATAR_ID, 1, false, true, true, 4, false, true),
                player(BOT_TWO_ID, BOT_TWO_NAME, BOT_TWO_AVATAR_ID, 2, false, true, true, 4, false, true),
                player(BOT_THREE_ID, BOT_THREE_NAME, BOT_THREE_AVATAR_ID, 3, false, true, true, 4, false, true)
        );

        GameSessionState.RoomState roomState = new GameSessionState.RoomState(
                ROOM_CODE,
                GameSessionState.Phase.JUDGING,
                GameSessionState.Mode.CLASSIC,
                TARGET_SCORE,
                5L,
                LOCAL_PLAYER_ID,
                false,
                players.size(),
                players.size(),
                players,
                new GameSessionState.Viewer(
                        LOCAL_PLAYER_ID,
                        false,
                        false,
                        false,
                        false,
                        true,
                        List.of()
                ),
                new GameSessionState.Round(
                        "demo_match",
                        "demo_round_04",
                        4,
                        LOCAL_PLAYER_ID,
                        new GameSessionState.Prompt("demo_prompt_04", "That's right, I killed _____. How, you ask? _____.", 2),
                        3,
                        3,
                        List.of(
                                submission(
                                        SUBMISSION_ALPHA,
                                        List.of("demo_bot_card_01", "demo_bot_card_02"),
                                        "Famine.\n\nFlying sex snakes.",
                                        null,
                                        null,
                                        false
                                ),
                                submission(
                                        SUBMISSION_BETA,
                                        List.of("demo_bot_card_03", "demo_bot_card_04"),
                                        "A live studio audience.\n\n72 virgins.",
                                        null,
                                        null,
                                        false
                                ),
                                submission(
                                        SUBMISSION_GAMMA,
                                        List.of("demo_bot_card_05", "demo_bot_card_06"),
                                        "Jennifer Lawrence.\n\nSilence.",
                                        null,
                                        null,
                                        false
                                )
                        ),
                        null,
                        null
                ),
                scoreboard(
                        score(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 4),
                        score(BOT_ONE_ID, BOT_ONE_NAME, BOT_ONE_AVATAR_ID, 4),
                        score(BOT_TWO_ID, BOT_TWO_NAME, BOT_TWO_AVATAR_ID, 4),
                        score(BOT_THREE_ID, BOT_THREE_NAME, BOT_THREE_AVATAR_ID, 4)
                )
        );

        return state(roomState, null, "Pick a winner to finish the offline demo match.");
    }

    GameSessionState finalScoreboardState(String winningSubmissionId) {
        String winningPlayerId = awardedPlayerIdFor(winningSubmissionId);
        String winningPlayerName = displayNameFor(winningPlayerId);

        List<GameSessionState.Score> scoreboard = scoreboard(
                score(winningPlayerId, winningPlayerName, avatarIdFor(winningPlayerId), 5),
                score(LOCAL_PLAYER_ID, LOCAL_PLAYER_NAME, LOCAL_AVATAR_ID, 4),
                score(nonWinningBotOne(winningPlayerId), displayNameFor(nonWinningBotOne(winningPlayerId)), avatarIdFor(nonWinningBotOne(winningPlayerId)), 4),
                score(nonWinningBotTwo(winningPlayerId), displayNameFor(nonWinningBotTwo(winningPlayerId)), avatarIdFor(nonWinningBotTwo(winningPlayerId)), 4)
        );

        List<GameSessionState.Player> players = playersFromScoreboard(scoreboard, winningPlayerId);

        GameSessionState.RoomState roomState = new GameSessionState.RoomState(
                ROOM_CODE,
                GameSessionState.Phase.GAME_OVER,
                GameSessionState.Mode.CLASSIC,
                TARGET_SCORE,
                6L,
                LOCAL_PLAYER_ID,
                false,
                players.size(),
                players.size(),
                players,
                new GameSessionState.Viewer(
                        LOCAL_PLAYER_ID,
                        false,
                        false,
                        false,
                        false,
                        false,
                        List.of()
                ),
                new GameSessionState.Round(
                        "demo_match",
                        "demo_round_04",
                        4,
                        LOCAL_PLAYER_ID,
                        new GameSessionState.Prompt("demo_prompt_04", "That's right, I killed _____. How, you ask? _____.", 2),
                        3,
                        3,
                        List.of(
                                visibleJudgedSubmission(
                                        SUBMISSION_ALPHA,
                                        List.of("demo_bot_card_01", "demo_bot_card_02"),
                                        "Famine.\n\nFlying sex snakes.",
                                        BOT_ONE_ID,
                                        BOT_ONE_NAME,
                                        SUBMISSION_ALPHA.equals(winningSubmissionId)
                                ),
                                visibleJudgedSubmission(
                                        SUBMISSION_BETA,
                                        List.of("demo_bot_card_03", "demo_bot_card_04"),
                                        "A live studio audience.\n\n72 virgins.",
                                        BOT_TWO_ID,
                                        BOT_TWO_NAME,
                                        SUBMISSION_BETA.equals(winningSubmissionId)
                                ),
                                visibleJudgedSubmission(
                                        SUBMISSION_GAMMA,
                                        List.of("demo_bot_card_05", "demo_bot_card_06"),
                                        "Jennifer Lawrence.\n\nSilence.",
                                        BOT_THREE_ID,
                                        BOT_THREE_NAME,
                                        SUBMISSION_GAMMA.equals(winningSubmissionId)
                                )
                        ),
                        winningSubmissionId,
                        winningPlayerId
                ),
                scoreboard
        );

        return state(roomState, null, "Offline demo complete. The final standings are fully local.");
    }

    private GameSessionState state(
            GameSessionState.RoomState roomState,
            GameSessionState.RoundReveal roundReveal,
            String statusMessage
    ) {
        return new GameSessionState(
                ConnectionStatus.CONNECTED,
                false,
                statusMessage,
                null,
                DEMO_SESSION,
                roomState,
                roundReveal
        );
    }

    private List<GameSessionState.Player> playersFromScoreboard(List<GameSessionState.Score> scoreboard, String winnerPlayerId) {
        List<GameSessionState.Player> players = new ArrayList<>();
        for (GameSessionState.Score score : scoreboard) {
            players.add(new GameSessionState.Player(
                    score.getPlayerId(),
                    score.getDisplayName(),
                    score.getAvatarId(),
                    players.size(),
                    LOCAL_PLAYER_ID.equals(score.getPlayerId()),
                    true,
                    true,
                    score.getScore(),
                    LOCAL_PLAYER_ID.equals(score.getPlayerId()),
                    false
            ));
        }
        players.sort(Comparator.comparingInt(GameSessionState.Player::getSeatIndex));
        return players;
    }

    private List<GameSessionState.Score> scoreboard(GameSessionState.Score... scores) {
        List<GameSessionState.Score> ordered = new ArrayList<>(List.of(scores));
        ordered.sort(Comparator.comparingInt(GameSessionState.Score::getScore).reversed()
                .thenComparing(GameSessionState.Score::getDisplayName));
        List<GameSessionState.Score> ranked = new ArrayList<>();
        for (int index = 0; index < ordered.size(); index++) {
            GameSessionState.Score score = ordered.get(index);
            ranked.add(new GameSessionState.Score(
                    score.getPlayerId(),
                    score.getDisplayName(),
                    score.getAvatarId(),
                    score.getScore(),
                    index + 1
            ));
        }
        return List.copyOf(ranked);
    }

    private GameSessionState.Score score(String playerId, String displayName, String avatarId, int score) {
        return new GameSessionState.Score(playerId, displayName, avatarId, score, 0);
    }

    private GameSessionState.Player player(
            String playerId,
            String displayName,
            String avatarId,
            int seatIndex,
            boolean host,
            boolean ready,
            boolean connected,
            int score,
            boolean judge,
            boolean submitted
    ) {
        return new GameSessionState.Player(
                playerId,
                displayName,
                avatarId,
                seatIndex,
                host,
                ready,
                connected,
                score,
                judge,
                submitted
        );
    }

    private GameSessionState.AnswerCard answer(String cardId, String text) {
        return new GameSessionState.AnswerCard(cardId, text);
    }

    private GameSessionState.Submission submission(
            String submissionId,
            String cardId,
            String text,
            String playerId,
            String submittedByName,
            boolean winner
    ) {
        return submission(submissionId, List.of(cardId), text, playerId, submittedByName, winner);
    }

    private GameSessionState.Submission submission(
            String submissionId,
            List<String> cardIds,
            String text,
            String playerId,
            String submittedByName,
            boolean winner
    ) {
        return new GameSessionState.Submission(
                submissionId,
                cardIds,
                text,
                playerId,
                submittedByName,
                winner,
                playerId == null ? null : "PLAYER"
        );
    }

    private GameSessionState.Submission visibleJudgedSubmission(
            String submissionId,
            String text,
            String playerId,
            String playerName,
            boolean winner
    ) {
        return visibleJudgedSubmission(submissionId, List.of(submissionId + "_card"), text, playerId, playerName, winner);
    }

    private GameSessionState.Submission visibleJudgedSubmission(
            String submissionId,
            List<String> cardIds,
            String text,
            String playerId,
            String playerName,
            boolean winner
    ) {
        return new GameSessionState.Submission(
                submissionId,
                cardIds,
                text,
                playerId,
                playerName,
                winner,
                "PLAYER"
        );
    }

    private String awardedPlayerIdFor(String submissionId) {
        if (SUBMISSION_ALPHA.equals(submissionId)) {
            return BOT_ONE_ID;
        }
        if (SUBMISSION_GAMMA.equals(submissionId)) {
            return BOT_THREE_ID;
        }
        return BOT_TWO_ID;
    }

    private String nonWinningBotOne(String winningPlayerId) {
        if (!BOT_ONE_ID.equals(winningPlayerId)) {
            return BOT_ONE_ID;
        }
        return BOT_TWO_ID;
    }

    private String nonWinningBotTwo(String winningPlayerId) {
        if (!BOT_THREE_ID.equals(winningPlayerId)) {
            return BOT_THREE_ID;
        }
        return BOT_TWO_ID;
    }

    private String displayNameFor(String playerId) {
        if (LOCAL_PLAYER_ID.equals(playerId)) {
            return LOCAL_PLAYER_NAME;
        }
        if (BOT_ONE_ID.equals(playerId)) {
            return BOT_ONE_NAME;
        }
        if (BOT_TWO_ID.equals(playerId)) {
            return BOT_TWO_NAME;
        }
        return BOT_THREE_NAME;
    }

    private String avatarIdFor(String playerId) {
        if (LOCAL_PLAYER_ID.equals(playerId)) {
            return LOCAL_AVATAR_ID;
        }
        if (BOT_ONE_ID.equals(playerId)) {
            return BOT_ONE_AVATAR_ID;
        }
        if (BOT_TWO_ID.equals(playerId)) {
            return BOT_TWO_AVATAR_ID;
        }
        return BOT_THREE_AVATAR_ID;
    }
}
