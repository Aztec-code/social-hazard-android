package com.socialhazard.app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameSessionState {

    private final ConnectionStatus connectionStatus;
    private final boolean loading;
    private final String statusMessage;
    private final String errorMessage;
    private final RoomSession roomSession;
    private final RoomState roomState;
    private final RoundReveal roundReveal;

    public GameSessionState(
            ConnectionStatus connectionStatus,
            boolean loading,
            String statusMessage,
            String errorMessage,
            RoomSession roomSession,
            RoomState roomState,
            RoundReveal roundReveal
    ) {
        this.connectionStatus = connectionStatus;
        this.loading = loading;
        this.statusMessage = statusMessage;
        this.errorMessage = errorMessage;
        this.roomSession = roomSession;
        this.roomState = roomState;
        this.roundReveal = roundReveal;
    }

    public static GameSessionState idle(RoomSession roomSession) {
        return new GameSessionState(
                ConnectionStatus.DISCONNECTED,
                false,
                roomSession == null ? null : "Saved room found. Reconnect when the app resumes.",
                null,
                roomSession,
                null,
                null
        );
    }

    public Builder buildUpon() {
        return new Builder(this);
    }

    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public boolean isLoading() {
        return loading;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public RoomSession getRoomSession() {
        return roomSession;
    }

    public RoomState getRoomState() {
        return roomState;
    }

    public RoundReveal getRoundReveal() {
        return roundReveal;
    }

    public boolean hasActiveRoom() {
        return roomSession != null && roomSession.getRoomCode() != null && !roomSession.getRoomCode().isBlank();
    }

    public static final class Builder {
        private ConnectionStatus connectionStatus;
        private boolean loading;
        private String statusMessage;
        private String errorMessage;
        private RoomSession roomSession;
        private RoomState roomState;
        private RoundReveal roundReveal;

        public Builder(GameSessionState state) {
            connectionStatus = state.connectionStatus;
            loading = state.loading;
            statusMessage = state.statusMessage;
            errorMessage = state.errorMessage;
            roomSession = state.roomSession;
            roomState = state.roomState;
            roundReveal = state.roundReveal;
        }

        public Builder setConnectionStatus(ConnectionStatus connectionStatus) {
            this.connectionStatus = connectionStatus;
            return this;
        }

        public Builder setLoading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder setRoomSession(RoomSession roomSession) {
            this.roomSession = roomSession;
            return this;
        }

        public Builder setRoomState(RoomState roomState) {
            this.roomState = roomState;
            return this;
        }

        public Builder setRoundReveal(RoundReveal roundReveal) {
            this.roundReveal = roundReveal;
            return this;
        }

        public GameSessionState build() {
            return new GameSessionState(
                    connectionStatus,
                    loading,
                    statusMessage,
                    errorMessage,
                    roomSession,
                    roomState,
                    roundReveal
            );
        }
    }

    public enum Phase {
        LOBBY,
        SUBMITTING,
        JUDGING,
        ROUND_RESULT,
        GAME_OVER,
        UNKNOWN;

        public static Phase fromRaw(String raw) {
            if (raw == null) {
                return UNKNOWN;
            }
            switch (raw) {
                case "LOBBY":
                    return LOBBY;
                case "SUBMITTING":
                    return SUBMITTING;
                case "JUDGING":
                    return JUDGING;
                case "ROUND_RESULT":
                    return ROUND_RESULT;
                case "GAME_OVER":
                    return GAME_OVER;
                default:
                    return UNKNOWN;
            }
        }
    }

    public enum Mode {
        DUEL,
        CLASSIC,
        UNKNOWN;

        public static Mode fromRaw(String raw) {
            if (raw == null) {
                return UNKNOWN;
            }
            switch (raw) {
                case "DUEL":
                    return DUEL;
                case "CLASSIC":
                    return CLASSIC;
                default:
                    return UNKNOWN;
            }
        }
    }

    public static final class RoomState {
        private final String roomCode;
        private final Phase phase;
        private final Mode mode;
        private final int targetScore;
        private final long stateVersion;
        private final String hostPlayerId;
        private final boolean canStart;
        private final int playerCount;
        private final int connectedPlayers;
        private final List<Player> players;
        private final Viewer viewer;
        private final Round round;
        private final List<Score> scoreboard;

        public RoomState(
                String roomCode,
                Phase phase,
                Mode mode,
                int targetScore,
                long stateVersion,
                String hostPlayerId,
                boolean canStart,
                int playerCount,
                int connectedPlayers,
                List<Player> players,
                Viewer viewer,
                Round round,
                List<Score> scoreboard
        ) {
            this.roomCode = roomCode;
            this.phase = phase;
            this.mode = mode;
            this.targetScore = targetScore;
            this.stateVersion = stateVersion;
            this.hostPlayerId = hostPlayerId;
            this.canStart = canStart;
            this.playerCount = playerCount;
            this.connectedPlayers = connectedPlayers;
            this.players = unmodifiableCopy(players);
            this.viewer = viewer;
            this.round = round;
            this.scoreboard = unmodifiableCopy(scoreboard);
        }

        public String getRoomCode() {
            return roomCode;
        }

        public Phase getPhase() {
            return phase;
        }

        public Mode getMode() {
            return mode;
        }

        public int getTargetScore() {
            return targetScore;
        }

        public long getStateVersion() {
            return stateVersion;
        }

        public String getHostPlayerId() {
            return hostPlayerId;
        }

        public boolean isCanStart() {
            return canStart;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public int getConnectedPlayers() {
            return connectedPlayers;
        }

        public List<Player> getPlayers() {
            return players;
        }

        public Viewer getViewer() {
            return viewer;
        }

        public Round getRound() {
            return round;
        }

        public List<Score> getScoreboard() {
            return scoreboard;
        }

        public Player findPlayer(String playerId) {
            if (playerId == null) {
                return null;
            }
            for (Player player : players) {
                if (playerId.equals(player.getPlayerId())) {
                    return player;
                }
            }
            return null;
        }
    }

    public static final class Player {
        private final String playerId;
        private final String displayName;
        private final String avatarId;
        private final int seatIndex;
        private final boolean host;
        private final boolean ready;
        private final boolean connected;
        private final int score;
        private final boolean judge;
        private final boolean submitted;

        public Player(
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
            this.playerId = playerId;
            this.displayName = displayName;
            this.avatarId = avatarId;
            this.seatIndex = seatIndex;
            this.host = host;
            this.ready = ready;
            this.connected = connected;
            this.score = score;
            this.judge = judge;
            this.submitted = submitted;
        }

        public String getPlayerId() {
            return playerId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAvatarId() {
            return avatarId;
        }

        public int getSeatIndex() {
            return seatIndex;
        }

        public boolean isHost() {
            return host;
        }

        public boolean isReady() {
            return ready;
        }

        public boolean isConnected() {
            return connected;
        }

        public int getScore() {
            return score;
        }

        public boolean isJudge() {
            return judge;
        }

        public boolean isSubmitted() {
            return submitted;
        }
    }

    public static final class Viewer {
        private final String playerId;
        private final boolean canStartGame;
        private final boolean canToggleReady;
        private final boolean canRename;
        private final boolean canSubmitCard;
        private final boolean canJudgePick;
        private final List<AnswerCard> hand;

        public Viewer(
                String playerId,
                boolean canStartGame,
                boolean canToggleReady,
                boolean canRename,
                boolean canSubmitCard,
                boolean canJudgePick,
                List<AnswerCard> hand
        ) {
            this.playerId = playerId;
            this.canStartGame = canStartGame;
            this.canToggleReady = canToggleReady;
            this.canRename = canRename;
            this.canSubmitCard = canSubmitCard;
            this.canJudgePick = canJudgePick;
            this.hand = unmodifiableCopy(hand);
        }

        public String getPlayerId() {
            return playerId;
        }

        public boolean isCanStartGame() {
            return canStartGame;
        }

        public boolean isCanToggleReady() {
            return canToggleReady;
        }

        public boolean isCanRename() {
            return canRename;
        }

        public boolean isCanSubmitCard() {
            return canSubmitCard;
        }

        public boolean isCanJudgePick() {
            return canJudgePick;
        }

        public List<AnswerCard> getHand() {
            return hand;
        }
    }

    public static final class Round {
        private final String matchId;
        private final String roundId;
        private final int roundNumber;
        private final String judgePlayerId;
        private final Prompt prompt;
        private final int requiredSubmissions;
        private final int receivedSubmissions;
        private final List<Submission> submissions;
        private final String winningSubmissionId;
        private final String winnerPlayerId;

        public Round(
                String matchId,
                String roundId,
                int roundNumber,
                String judgePlayerId,
                Prompt prompt,
                int requiredSubmissions,
                int receivedSubmissions,
                List<Submission> submissions,
                String winningSubmissionId,
                String winnerPlayerId
        ) {
            this.matchId = matchId;
            this.roundId = roundId;
            this.roundNumber = roundNumber;
            this.judgePlayerId = judgePlayerId;
            this.prompt = prompt;
            this.requiredSubmissions = requiredSubmissions;
            this.receivedSubmissions = receivedSubmissions;
            this.submissions = unmodifiableCopy(submissions);
            this.winningSubmissionId = winningSubmissionId;
            this.winnerPlayerId = winnerPlayerId;
        }

        public String getMatchId() {
            return matchId;
        }

        public String getRoundId() {
            return roundId;
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public String getJudgePlayerId() {
            return judgePlayerId;
        }

        public Prompt getPrompt() {
            return prompt;
        }

        public int getRequiredSubmissions() {
            return requiredSubmissions;
        }

        public int getReceivedSubmissions() {
            return receivedSubmissions;
        }

        public List<Submission> getSubmissions() {
            return submissions;
        }

        public String getWinningSubmissionId() {
            return winningSubmissionId;
        }

        public String getWinnerPlayerId() {
            return winnerPlayerId;
        }
    }

    public static final class Prompt {
        private final String promptId;
        private final String text;
        private final int pickCount;

        public Prompt(String promptId, String text, int pickCount) {
            this.promptId = promptId;
            this.text = text;
            this.pickCount = pickCount;
        }

        public String getPromptId() {
            return promptId;
        }

        public String getText() {
            return text;
        }

        public int getPickCount() {
            return pickCount;
        }
    }

    public static final class AnswerCard {
        private final String cardId;
        private final String text;

        public AnswerCard(String cardId, String text) {
            this.cardId = cardId;
            this.text = text;
        }

        public String getCardId() {
            return cardId;
        }

        public String getText() {
            return text;
        }
    }

    public static final class Submission {
        private final String submissionId;
        private final List<String> cardIds;
        private final String text;
        private final String submittedByPlayerId;
        private final String submittedByName;
        private final boolean winner;
        private final String origin;

        public Submission(
                String submissionId,
                List<String> cardIds,
                String text,
                String submittedByPlayerId,
                String submittedByName,
                boolean winner,
                String origin
        ) {
            this.submissionId = submissionId;
            this.cardIds = unmodifiableCopy(cardIds);
            this.text = text;
            this.submittedByPlayerId = submittedByPlayerId;
            this.submittedByName = submittedByName;
            this.winner = winner;
            this.origin = origin;
        }

        public String getSubmissionId() {
            return submissionId;
        }

        public List<String> getCardIds() {
            return cardIds;
        }

        public String getText() {
            return text;
        }

        public String getSubmittedByPlayerId() {
            return submittedByPlayerId;
        }

        public String getSubmittedByName() {
            return submittedByName;
        }

        public boolean isWinner() {
            return winner;
        }

        public String getOrigin() {
            return origin;
        }
    }

    public static final class Score {
        private final String playerId;
        private final String displayName;
        private final String avatarId;
        private final int score;
        private final int rank;

        public Score(String playerId, String displayName, String avatarId, int score, int rank) {
            this.playerId = playerId;
            this.displayName = displayName;
            this.avatarId = avatarId;
            this.score = score;
            this.rank = rank;
        }

        public String getPlayerId() {
            return playerId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAvatarId() {
            return avatarId;
        }

        public int getScore() {
            return score;
        }

        public int getRank() {
            return rank;
        }
    }

    public static final class RoundReveal {
        private final int roundNumber;
        private final String winnerName;
        private final String winningAnswer;
        private final List<Score> scoreboard;

        public RoundReveal(int roundNumber, String winnerName, String winningAnswer, List<Score> scoreboard) {
            this.roundNumber = roundNumber;
            this.winnerName = winnerName;
            this.winningAnswer = winningAnswer;
            this.scoreboard = unmodifiableCopy(scoreboard);
        }

        public int getRoundNumber() {
            return roundNumber;
        }

        public String getWinnerName() {
            return winnerName;
        }

        public String getWinningAnswer() {
            return winningAnswer;
        }

        public List<Score> getScoreboard() {
            return scoreboard;
        }
    }

    private static <T> List<T> unmodifiableCopy(List<T> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
