package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.socialhazard.app.model.ConnectionStatus;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.SubmissionCardUiModel;
import com.socialhazard.app.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

public final class JudgeViewModel extends BaseViewModel {

    private final GameRepository repository;
    private final MediatorLiveData<UiState> uiState = new MediatorLiveData<>();
    private String selectedSubmissionId;

    public JudgeViewModel(GameRepository repository) {
        this.repository = repository;
        uiState.addSource(repository.observeSessionState(), this::publish);
        uiState.addSource(repository.observeDemoActive(), value -> publish(repository.observeSessionState().getValue()));
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void selectSubmission(String submissionId) {
        selectedSubmissionId = submissionId;
        publish(repository.observeSessionState().getValue());
    }

    public void confirmWinner() {
        GameSessionState state = repository.observeSessionState().getValue();
        if (state == null) {
            return;
        }
        if (state.getConnectionStatus() != ConnectionStatus.CONNECTED) {
            repository.resumeSession();
            return;
        }
        if (selectedSubmissionId != null) {
            repository.judgePick(selectedSubmissionId);
        }
    }

    public void leaveRoom() {
        repository.leaveRoom();
    }

    private void publish(GameSessionState sessionState) {
        GameSessionState.RoomState roomState = sessionState == null ? null : sessionState.getRoomState();
        GameSessionState.Round round = roomState == null ? null : roomState.getRound();
        GameSessionState.Viewer viewer = roomState == null ? null : roomState.getViewer();
        GameSessionState.Player judge = roomState == null || round == null ? null : roomState.findPlayer(round.getJudgePlayerId());

        List<SubmissionCardUiModel> submissions = buildSubmissions(round);
        if (selectedSubmissionId != null && submissions.stream().noneMatch(item -> selectedSubmissionId.equals(item.getId()))) {
            selectedSubmissionId = null;
            submissions = buildSubmissions(round);
        }

        Boolean demoActive = repository.observeDemoActive().getValue();
        uiState.setValue(new UiState(
                judge == null ? "Waiting for judge..." : "Judge: " + judge.getDisplayName(),
                round == null || round.getPrompt() == null ? "Waiting for the next prompt..." : round.getPrompt().getText(),
                submissions,
                buildSubmissionStateMessage(sessionState, viewer, submissions),
                buildPrimaryLabel(sessionState, viewer),
                isPrimaryEnabled(sessionState, viewer),
                demoActive != null && demoActive
        ));
    }

    private List<SubmissionCardUiModel> buildSubmissions(GameSessionState.Round round) {
        List<SubmissionCardUiModel> submissions = new ArrayList<>();
        if (round == null) {
            return submissions;
        }
        for (int index = 0; index < round.getSubmissions().size(); index++) {
            GameSessionState.Submission submission = round.getSubmissions().get(index);
            submissions.add(new SubmissionCardUiModel(
                    submission.getSubmissionId(),
                    "Answer " + (char) ('A' + index),
                    submission.getText(),
                    submission.getSubmissionId().equals(selectedSubmissionId)
            ));
        }
        return submissions;
    }

    private String buildPrimaryLabel(GameSessionState sessionState, GameSessionState.Viewer viewer) {
        if (sessionState == null) {
            return "Reconnect";
        }
        if (sessionState.isLoading()) {
            return "Working...";
        }
        if (sessionState.getConnectionStatus() == ConnectionStatus.ERROR) {
            return "Retry connection";
        }
        if (sessionState.getConnectionStatus() != ConnectionStatus.CONNECTED) {
            return "Reconnect";
        }
        if (viewer == null || !viewer.isCanJudgePick()) {
            return "Waiting for judge";
        }
        return selectedSubmissionId == null ? "Pick a winner" : "Choose winner";
    }

    private String buildSubmissionStateMessage(
            GameSessionState sessionState,
            GameSessionState.Viewer viewer,
            List<SubmissionCardUiModel> submissions
    ) {
        if (sessionState == null) {
            return "Waiting for room state.";
        }
        if (sessionState.getErrorMessage() != null && !sessionState.getErrorMessage().isBlank()) {
            return sessionState.getErrorMessage();
        }
        if (sessionState.isLoading() || sessionState.getConnectionStatus() != ConnectionStatus.CONNECTED) {
            return "Refreshing the judge queue from the server.";
        }
        if (viewer == null || !viewer.isCanJudgePick()) {
            return "Submissions stay hidden until it is your judging turn.";
        }
        if (submissions.isEmpty()) {
            return "Waiting for every non-judge player to submit.";
        }
        return null;
    }

    private boolean isPrimaryEnabled(GameSessionState sessionState, GameSessionState.Viewer viewer) {
        return sessionState != null
                && sessionState.getConnectionStatus() == ConnectionStatus.CONNECTED
                && !sessionState.isLoading()
                && viewer != null
                && viewer.isCanJudgePick()
                && selectedSubmissionId != null;
    }

    public static final class UiState {
        private final String judgeLabel;
        private final String promptText;
        private final List<SubmissionCardUiModel> submissions;
        private final String submissionStateMessage;
        private final String primaryLabel;
        private final boolean primaryEnabled;
        private final boolean demoActive;

        public UiState(
                String judgeLabel,
                String promptText,
                List<SubmissionCardUiModel> submissions,
                String submissionStateMessage,
                String primaryLabel,
                boolean primaryEnabled,
                boolean demoActive
        ) {
            this.judgeLabel = judgeLabel;
            this.promptText = promptText;
            this.submissions = submissions;
            this.submissionStateMessage = submissionStateMessage;
            this.primaryLabel = primaryLabel;
            this.primaryEnabled = primaryEnabled;
            this.demoActive = demoActive;
        }

        public String getJudgeLabel() {
            return judgeLabel;
        }

        public String getPromptText() {
            return promptText;
        }

        public List<SubmissionCardUiModel> getSubmissions() {
            return submissions;
        }

        public String getSubmissionStateMessage() {
            return submissionStateMessage;
        }

        public String getPrimaryLabel() {
            return primaryLabel;
        }

        public boolean isPrimaryEnabled() {
            return primaryEnabled;
        }

        public boolean isDemoActive() {
            return demoActive;
        }
    }
}
