package com.socialhazard.server.service;

import com.socialhazard.server.config.GameServerProperties;
import com.socialhazard.server.exception.GameException;
import com.socialhazard.server.model.domain.AnswerCard;
import com.socialhazard.server.model.domain.CardSubmission;
import com.socialhazard.server.model.domain.DeckState;
import com.socialhazard.server.model.domain.GameMode;
import com.socialhazard.server.model.domain.GameRoom;
import com.socialhazard.server.model.domain.PromptCard;
import com.socialhazard.server.model.domain.RoomPhase;
import com.socialhazard.server.model.domain.RoomPlayer;
import com.socialhazard.server.model.domain.SubmissionOrigin;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MatchEngine {

    private final GameServerProperties properties;
    private final OriginalCardCatalog cardCatalog;
    private final GameIdGenerator idGenerator;
    private final Random random = new SecureRandom();

    public MatchEngine(
            GameServerProperties properties,
            OriginalCardCatalog cardCatalog,
            GameIdGenerator idGenerator
    ) {
        this.properties = properties;
        this.cardCatalog = cardCatalog;
        this.idGenerator = idGenerator;
    }

    public void startMatch(GameRoom room, Instant now) {
        int playerCount = room.getPlayerCount();
        if (playerCount < properties.getMinPlayers() || playerCount > properties.getMaxPlayers()) {
            throw new GameException("ROOM_NOT_READY", "Only rooms with 3 to 4 players can start a match.");
        }

        GameMode mode = GameMode.CLASSIC;
        DeckState<PromptCard> promptDeck = new DeckState<>(cardCatalog.promptCards(), random);
        DeckState<AnswerCard> answerDeck = new DeckState<>(cardCatalog.answerCards(), random);
        room.initializeMatch(mode, idGenerator.newMatchId(), handSizeFor(mode), promptDeck, answerDeck, now);
        refillHands(room, now);
        startNextRound(room, now);
    }

    public boolean submitCard(GameRoom room, RoomPlayer player, String roundId, List<String> cardIds, Instant now) {
        requirePhase(room, RoomPhase.SUBMITTING, "SUBMISSION_WINDOW_CLOSED", "This round is not accepting submissions.");
        requireRound(room, roundId);

        if (player.getPlayerId().equals(room.getJudgePlayerId())) {
            throw new GameException("JUDGE_CANNOT_SUBMIT", "The judge cannot submit a card this round.");
        }
        if (room.hasPlayerSubmitted(player.getPlayerId())) {
            throw new GameException("ALREADY_SUBMITTED", "This player has already submitted a card this round.");
        }
        int requiredPickCount = room.getPromptCard() == null ? 1 : room.getPromptCard().pickCount();
        if (cardIds == null || cardIds.size() != requiredPickCount) {
            throw new GameException("INVALID_SUBMISSION", "This black card requires " + requiredPickCount + " blue card(s).");
        }

        List<AnswerCard> answerCards = player.removeFromHand(cardIds, now)
                .orElseThrow(() -> new GameException("CARD_NOT_IN_HAND", "One or more selected cards are not in the player's hand."));
        answerCards.forEach(room.getAnswerDeck()::discard);
        room.addSubmission(new CardSubmission(
                idGenerator.newSubmissionId(),
                player.getPlayerId(),
                player.getDisplayName(),
                answerCards,
                SubmissionOrigin.PLAYER
        ), now);

        if (room.getPlayerSubmissions().size() < room.getRequiredSubmissionCount()) {
            return false;
        }

        List<CardSubmission> judgeChoices = new ArrayList<>(room.getPlayerSubmissions());
        shuffle(judgeChoices);
        room.enterJudging(judgeChoices, now);
        return true;
    }

    public JudgeDecision resolveJudgePick(
            GameRoom room,
            RoomPlayer judge,
            String roundId,
            String submissionId,
            Instant now
    ) {
        requirePhase(room, RoomPhase.JUDGING, "JUDGE_WINDOW_CLOSED", "The round is not currently waiting on the judge.");
        requireRound(room, roundId);

        if (!judge.getPlayerId().equals(room.getJudgePlayerId())) {
            throw new GameException("NOT_JUDGE", "Only the active judge can choose the winning submission.");
        }

        CardSubmission winningSubmission = room.getJudgeChoices().stream()
                .filter(submission -> submission.getSubmissionId().equals(submissionId))
                .findFirst()
                .orElseThrow(() -> new GameException("SUBMISSION_NOT_FOUND", "The chosen submission does not exist in this round."));
        winningSubmission.markWinner();

        String awardedPlayerId = winningSubmission.getSubmittedByPlayerId();

        RoomPlayer scoringPlayer = room.findPlayer(awardedPlayerId)
                .orElseThrow(() -> new GameException("PLAYER_NOT_FOUND", "Unable to find the round winner in the room."));
        scoringPlayer.incrementScore(now);

        boolean gameOver = scoringPlayer.getScore() >= room.getTargetScore();
        if (gameOver) {
            room.finishGame(awardedPlayerId, winningSubmission.getSubmissionId(), now);
        } else {
            room.enterRoundResult(awardedPlayerId, winningSubmission.getSubmissionId(), now);
        }

        return new JudgeDecision(
                awardedPlayerId,
                winningSubmission.getSubmissionId(),
                false,
                gameOver
        );
    }

    public void advanceRound(GameRoom room, Instant now) {
        requirePhase(room, RoomPhase.ROUND_RESULT, "ROUND_NOT_READY", "The room is not ready to advance.");
        room.getPromptDeck().discard(room.getPromptCard());
        refillHands(room, now);
        startNextRound(room, now);
    }

    public void cancelMatch(GameRoom room, Instant now) {
        if (room.getPhase() != RoomPhase.LOBBY) {
            room.resetToLobby(now);
        }
    }

    private void startNextRound(GameRoom room, Instant now) {
        List<RoomPlayer> players = room.getOrderedPlayers();
        int nextJudgeIndex = room.getJudgeIndex() < 0 ? 0 : (room.getJudgeIndex() + 1) % players.size();
        RoomPlayer judge = players.get(nextJudgeIndex);
        room.setJudgeIndex(nextJudgeIndex);
        room.beginRound(
                idGenerator.newRoundId(),
                judge.getPlayerId(),
                room.getPromptDeck().draw(),
                room.getRoundNumber() + 1,
                now
        );
    }

    private void refillHands(GameRoom room, Instant now) {
        for (RoomPlayer player : room.getPlayers()) {
            while (player.getHand().size() < room.getHandSize()) {
                player.addToHand(room.getAnswerDeck().draw(), now);
            }
        }
    }

    private int handSizeFor(GameMode mode) {
        return properties.getClassicHandSize();
    }

    private void requirePhase(GameRoom room, RoomPhase phase, String code, String message) {
        if (room.getPhase() != phase) {
            throw new GameException(code, message);
        }
    }

    private void requireRound(GameRoom room, String roundId) {
        if (roundId == null || !roundId.equals(room.getRoundId())) {
            throw new GameException("ROUND_NOT_ACTIVE", "The supplied round id does not match the active round.");
        }
    }

    private void shuffle(List<CardSubmission> submissions) {
        for (int index = submissions.size() - 1; index > 0; index--) {
            int swapIndex = random.nextInt(index + 1);
            CardSubmission value = submissions.get(index);
            submissions.set(index, submissions.get(swapIndex));
            submissions.set(swapIndex, value);
        }
    }

    public record JudgeDecision(
            String awardedPlayerId,
            String winningSubmissionId,
            boolean houseWon,
            boolean gameOver
    ) {
    }
}
