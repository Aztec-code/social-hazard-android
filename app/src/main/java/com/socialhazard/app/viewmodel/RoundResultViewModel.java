package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.ScoreRowUiModel;
import com.socialhazard.app.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

public final class RoundResultViewModel extends BaseViewModel {

    private final GameRepository repository;
    private final MediatorLiveData<UiState> uiState = new MediatorLiveData<>();

    public RoundResultViewModel(GameRepository repository) {
        this.repository = repository;
        uiState.addSource(repository.observeSessionState(), this::publish);
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void continueToNextScreen() {
        repository.dismissRoundReveal();
    }

    public void leaveRoom() {
        repository.leaveRoom();
    }

    private void publish(GameSessionState sessionState) {
        GameSessionState.RoundReveal roundReveal = sessionState == null ? null : sessionState.getRoundReveal();
        List<ScoreRowUiModel> scores = new ArrayList<>();
        if (roundReveal != null) {
            for (GameSessionState.Score score : roundReveal.getScoreboard()) {
                scores.add(new ScoreRowUiModel(
                        String.valueOf(score.getRank()),
                        score.getDisplayName(),
                        score.getAvatarId(),
                        score.getRank() == 1 ? "Leading the room" : "Still in striking distance",
                        score.getScore(),
                        score.getRank() == 1
                ));
            }
        }

        uiState.setValue(new UiState(
                roundReveal == null ? "Round winner" : roundReveal.getWinnerName(),
                roundReveal == null ? "Waiting for the winning card..." : roundReveal.getWinningAnswer(),
                sessionState == null || sessionState.getStatusMessage() == null
                        ? "The server has already prepared the next phase. Continue when you are ready."
                        : sessionState.getStatusMessage(),
                scores,
                scores.isEmpty() ? "Score changes will appear here once the round reveal is available." : null,
                "Continue"
        ));
    }

    public static final class UiState {
        private final String winnerName;
        private final String winningAnswer;
        private final String body;
        private final List<ScoreRowUiModel> scores;
        private final String scoreStateMessage;
        private final String primaryLabel;

        public UiState(
                String winnerName,
                String winningAnswer,
                String body,
                List<ScoreRowUiModel> scores,
                String scoreStateMessage,
                String primaryLabel
        ) {
            this.winnerName = winnerName;
            this.winningAnswer = winningAnswer;
            this.body = body;
            this.scores = scores;
            this.scoreStateMessage = scoreStateMessage;
            this.primaryLabel = primaryLabel;
        }

        public String getWinnerName() {
            return winnerName;
        }

        public String getWinningAnswer() {
            return winningAnswer;
        }

        public String getBody() {
            return body;
        }

        public List<ScoreRowUiModel> getScores() {
            return scores;
        }

        public String getScoreStateMessage() {
            return scoreStateMessage;
        }

        public String getPrimaryLabel() {
            return primaryLabel;
        }
    }
}
