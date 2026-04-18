package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.ScoreRowUiModel;
import com.socialhazard.app.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

public final class FinalScoreboardViewModel extends BaseViewModel {

    private final GameRepository repository;
    private final MediatorLiveData<UiState> uiState = new MediatorLiveData<>();

    public FinalScoreboardViewModel(GameRepository repository) {
        this.repository = repository;
        uiState.addSource(repository.observeSessionState(), this::publish);
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void leaveRoom() {
        repository.leaveRoom();
    }

    private void publish(GameSessionState sessionState) {
        GameSessionState.RoomState roomState = sessionState == null ? null : sessionState.getRoomState();
        List<ScoreRowUiModel> scores = new ArrayList<>();
        if (roomState != null) {
            for (GameSessionState.Score score : roomState.getScoreboard()) {
                scores.add(new ScoreRowUiModel(
                        String.valueOf(score.getRank()),
                        score.getDisplayName(),
                        score.getAvatarId(),
                        score.getRank() == 1 ? "Match winner" : "Finished strong",
                        score.getScore(),
                        score.getRank() == 1
                ));
            }
        }

        uiState.setValue(new UiState(
                "Match result",
                sessionState != null && sessionState.getStatusMessage() != null
                        ? sessionState.getStatusMessage()
                        : "Final standings come directly from the authoritative server state.",
                scores,
                scores.isEmpty() ? "The final standings will appear here once the match is complete." : null,
                "Leave room"
        ));
    }

    public static final class UiState {
        private final String title;
        private final String body;
        private final List<ScoreRowUiModel> scores;
        private final String scoreStateMessage;
        private final String primaryLabel;

        public UiState(String title, String body, List<ScoreRowUiModel> scores, String scoreStateMessage, String primaryLabel) {
            this.title = title;
            this.body = body;
            this.scores = scores;
            this.scoreStateMessage = scoreStateMessage;
            this.primaryLabel = primaryLabel;
        }

        public String getTitle() {
            return title;
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
