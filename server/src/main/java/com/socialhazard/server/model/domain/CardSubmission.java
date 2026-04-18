package com.socialhazard.server.model.domain;

import java.util.List;
import java.util.stream.Collectors;

public final class CardSubmission {

    private final String submissionId;
    private final String submittedByPlayerId;
    private final String submittedByName;
    private final List<AnswerCard> answerCards;
    private final SubmissionOrigin origin;
    private boolean winner;

    public CardSubmission(
            String submissionId,
            String submittedByPlayerId,
            String submittedByName,
            List<AnswerCard> answerCards,
            SubmissionOrigin origin
    ) {
        this.submissionId = submissionId;
        this.submittedByPlayerId = submittedByPlayerId;
        this.submittedByName = submittedByName;
        this.answerCards = List.copyOf(answerCards);
        this.origin = origin;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getSubmittedByPlayerId() {
        return submittedByPlayerId;
    }

    public String getSubmittedByName() {
        return submittedByName;
    }

    public List<AnswerCard> getAnswerCards() {
        return answerCards;
    }

    public String getDisplayText() {
        return answerCards.stream()
                .map(AnswerCard::text)
                .collect(Collectors.joining("\n\n"));
    }

    public SubmissionOrigin getOrigin() {
        return origin;
    }

    public boolean isWinner() {
        return winner;
    }

    public void markWinner() {
        this.winner = true;
    }
}
