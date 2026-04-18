package com.socialhazard.server.model.domain;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class RoomPlayer {

    private final String playerId;
    private final String playerToken;
    private final Instant joinedAt;
    private final List<AnswerCard> hand = new ArrayList<>();
    private String displayName;
    private String avatarId;
    private String sessionId;
    private boolean connected;
    private boolean ready;
    private int score;
    private Instant updatedAt;

    public RoomPlayer(
            String playerId,
            String displayName,
            String avatarId,
            String playerToken,
            String sessionId,
            Instant joinedAt
    ) {
        this.playerId = playerId;
        this.displayName = displayName;
        this.avatarId = avatarId;
        this.playerToken = playerToken;
        this.sessionId = sessionId;
        this.joinedAt = joinedAt;
        this.connected = true;
        this.ready = false;
        this.updatedAt = joinedAt;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isReady() {
        return ready;
    }

    public int getScore() {
        return score;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<AnswerCard> getHand() {
        return List.copyOf(hand);
    }

    public void attachToSession(String sessionId, Instant now) {
        this.sessionId = sessionId;
        this.connected = true;
        this.updatedAt = now;
    }

    public void markDisconnected(Instant now) {
        this.sessionId = null;
        this.connected = false;
        this.ready = false;
        this.updatedAt = now;
    }

    public void setReady(boolean ready, Instant now) {
        this.ready = ready;
        this.updatedAt = now;
    }

    public void resetReady(Instant now) {
        this.ready = false;
        this.updatedAt = now;
    }

    public void updateProfile(String displayName, String avatarId, Instant now) {
        this.displayName = displayName;
        this.avatarId = avatarId;
        this.updatedAt = now;
    }

    public void resetForMatch(Instant now) {
        this.ready = false;
        this.score = 0;
        this.hand.clear();
        this.updatedAt = now;
    }

    public void clearHand(Instant now) {
        this.hand.clear();
        this.updatedAt = now;
    }

    public void addToHand(AnswerCard answerCard, Instant now) {
        this.hand.add(answerCard);
        this.updatedAt = now;
    }

    public Optional<AnswerCard> removeFromHand(String cardId, Instant now) {
        for (int index = 0; index < hand.size(); index++) {
            AnswerCard answerCard = hand.get(index);
            if (answerCard.cardId().equals(cardId)) {
                hand.remove(index);
                this.updatedAt = now;
                return Optional.of(answerCard);
            }
        }
        return Optional.empty();
    }

    public Optional<List<AnswerCard>> removeFromHand(List<String> cardIds, Instant now) {
        if (cardIds == null || cardIds.isEmpty()) {
            return Optional.empty();
        }

        List<Integer> matchedIndexes = new ArrayList<>();
        List<AnswerCard> selectedCards = new ArrayList<>();
        for (String cardId : cardIds) {
            if (cardId == null || cardId.isBlank()) {
                return Optional.empty();
            }
            boolean matched = false;
            for (int index = 0; index < hand.size(); index++) {
                if (matchedIndexes.contains(index)) {
                    continue;
                }
                AnswerCard answerCard = hand.get(index);
                if (answerCard.cardId().equals(cardId)) {
                    matchedIndexes.add(index);
                    selectedCards.add(answerCard);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return Optional.empty();
            }
        }

        matchedIndexes.sort(java.util.Comparator.reverseOrder());
        for (int index : matchedIndexes) {
            hand.remove(index);
        }
        this.updatedAt = now;
        return Optional.of(List.copyOf(selectedCards));
    }

    public void incrementScore(Instant now) {
        this.score++;
        this.updatedAt = now;
    }
}
