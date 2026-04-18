package com.socialhazard.server.model.domain;

import java.util.ArrayList;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class GameRoom {

    private final String roomCode;
    private final Instant createdAt;
    private final Map<String, RoomPlayer> players = new LinkedHashMap<>();
    private RoomPhase phase = RoomPhase.LOBBY;
    private String hostPlayerId;
    private final int targetScore;
    private Instant updatedAt;
    private long stateVersion;
    private GameMode mode;
    private String matchId;
    private int handSize;
    private int roundNumber;
    private int judgeIndex = -1;
    private String judgePlayerId;
    private String roundId;
    private PromptCard promptCard;
    private DeckState<PromptCard> promptDeck;
    private DeckState<AnswerCard> answerDeck;
    private final List<CardSubmission> playerSubmissions = new ArrayList<>();
    private final List<CardSubmission> judgeChoices = new ArrayList<>();
    private String winnerPlayerId;
    private String winningSubmissionId;

    public GameRoom(String roomCode, String hostPlayerId, int targetScore, Instant createdAt) {
        this.roomCode = roomCode;
        this.hostPlayerId = hostPlayerId;
        this.targetScore = targetScore;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public RoomPhase getPhase() {
        return phase;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getStateVersion() {
        return stateVersion;
    }

    public GameMode getMode() {
        return mode;
    }

    public String getMatchId() {
        return matchId;
    }

    public int getHandSize() {
        return handSize;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public int getJudgeIndex() {
        return judgeIndex;
    }

    public String getJudgePlayerId() {
        return judgePlayerId;
    }

    public String getRoundId() {
        return roundId;
    }

    public PromptCard getPromptCard() {
        return promptCard;
    }

    public DeckState<PromptCard> getPromptDeck() {
        return promptDeck;
    }

    public DeckState<AnswerCard> getAnswerDeck() {
        return answerDeck;
    }

    public List<CardSubmission> getPlayerSubmissions() {
        return List.copyOf(playerSubmissions);
    }

    public List<CardSubmission> getJudgeChoices() {
        return List.copyOf(judgeChoices);
    }

    public String getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public String getWinningSubmissionId() {
        return winningSubmissionId;
    }

    public Collection<RoomPlayer> getPlayers() {
        return players.values();
    }

    public List<RoomPlayer> getOrderedPlayers() {
        return new ArrayList<>(players.values());
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getConnectedPlayerCount() {
        return (int) players.values().stream().filter(RoomPlayer::isConnected).count();
    }

    public boolean hasConnectedPlayers() {
        return players.values().stream().anyMatch(RoomPlayer::isConnected);
    }

    public void addPlayer(RoomPlayer player, Instant now) {
        players.put(player.getPlayerId(), player);
        touch(now);
    }

    public Optional<RoomPlayer> findPlayer(String playerId) {
        return Optional.ofNullable(players.get(playerId));
    }

    public void removePlayer(String playerId, Instant now) {
        players.remove(playerId);
        if (playerId.equals(hostPlayerId)) {
            hostPlayerId = players.values().stream()
                    .findFirst()
                    .map(RoomPlayer::getPlayerId)
                    .orElse(null);
        }
        touch(now);
    }

    public boolean isHost(String playerId) {
        return playerId != null && playerId.equals(hostPlayerId);
    }

    public boolean canStartMatch(int minPlayers, int maxPlayers) {
        if (phase != RoomPhase.LOBBY) {
            return false;
        }
        int playerCount = players.size();
        if (playerCount < minPlayers || playerCount > maxPlayers) {
            return false;
        }
        return players.values().stream().allMatch(player ->
                player.isConnected() && player.isReady()
        );
    }

    public boolean hasPlayerSubmitted(String playerId) {
        return playerSubmissions.stream().anyMatch(submission -> playerId.equals(submission.getSubmittedByPlayerId()));
    }

    public int getRequiredSubmissionCount() {
        return Math.max(0, players.size() - 1);
    }

    public void initializeMatch(
            GameMode mode,
            String matchId,
            int handSize,
            DeckState<PromptCard> promptDeck,
            DeckState<AnswerCard> answerDeck,
            Instant now
    ) {
        this.mode = mode;
        this.matchId = matchId;
        this.handSize = handSize;
        this.promptDeck = promptDeck;
        this.answerDeck = answerDeck;
        this.roundNumber = 0;
        this.judgeIndex = -1;
        this.judgePlayerId = null;
        this.roundId = null;
        this.promptCard = null;
        this.playerSubmissions.clear();
        this.judgeChoices.clear();
        this.winnerPlayerId = null;
        this.winningSubmissionId = null;
        this.phase = RoomPhase.LOBBY;
        players.values().forEach(player -> player.resetForMatch(now));
        touch(now);
    }

    public void beginRound(String roundId, String judgePlayerId, PromptCard promptCard, int roundNumber, Instant now) {
        this.roundId = roundId;
        this.judgePlayerId = judgePlayerId;
        this.promptCard = promptCard;
        this.roundNumber = roundNumber;
        this.playerSubmissions.clear();
        this.judgeChoices.clear();
        this.winnerPlayerId = null;
        this.winningSubmissionId = null;
        this.phase = RoomPhase.SUBMITTING;
        touch(now);
    }

    public void addSubmission(CardSubmission submission, Instant now) {
        this.playerSubmissions.add(submission);
        touch(now);
    }

    public void enterJudging(List<CardSubmission> choices, Instant now) {
        this.judgeChoices.clear();
        this.judgeChoices.addAll(choices);
        this.phase = RoomPhase.JUDGING;
        touch(now);
    }

    public void enterRoundResult(String winnerPlayerId, String winningSubmissionId, Instant now) {
        this.winnerPlayerId = winnerPlayerId;
        this.winningSubmissionId = winningSubmissionId;
        this.phase = RoomPhase.ROUND_RESULT;
        touch(now);
    }

    public void finishGame(String winnerPlayerId, String winningSubmissionId, Instant now) {
        this.winnerPlayerId = winnerPlayerId;
        this.winningSubmissionId = winningSubmissionId;
        this.phase = RoomPhase.GAME_OVER;
        touch(now);
    }

    public void setJudgeIndex(int judgeIndex) {
        this.judgeIndex = judgeIndex;
    }

    public void resetToLobby(Instant now) {
        this.phase = RoomPhase.LOBBY;
        this.mode = null;
        this.matchId = null;
        this.handSize = 0;
        this.roundNumber = 0;
        this.judgeIndex = -1;
        this.judgePlayerId = null;
        this.roundId = null;
        this.promptCard = null;
        this.promptDeck = null;
        this.answerDeck = null;
        this.playerSubmissions.clear();
        this.judgeChoices.clear();
        this.winnerPlayerId = null;
        this.winningSubmissionId = null;
        players.values().forEach(player -> player.resetForMatch(now));
        touch(now);
    }

    public void touch(Instant now) {
        this.updatedAt = now;
        this.stateVersion++;
    }

    public void forceUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
