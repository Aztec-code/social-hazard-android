package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.socialhazard.app.model.AnswerCardUiModel;
import com.socialhazard.app.model.ConnectionStatus;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

public final class GameViewModel extends BaseViewModel {

    private final GameRepository repository;
    private final MediatorLiveData<UiState> uiState = new MediatorLiveData<>();
    private final List<String> selectedCardIds = new ArrayList<>();

    public GameViewModel(GameRepository repository) {
        this.repository = repository;
        uiState.addSource(repository.observeSessionState(), this::publish);
        uiState.addSource(repository.observeDemoActive(), value -> publish(repository.observeSessionState().getValue()));
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void selectCard(String cardId) {
        if (cardId == null || cardId.isBlank()) {
            return;
        }
        int selectedIndex = selectedCardIds.indexOf(cardId);
        if (selectedIndex >= 0) {
            selectedCardIds.remove(selectedIndex);
            publish(repository.observeSessionState().getValue());
            return;
        }

        int requiredPickCount = requiredPickCount(repository.observeSessionState().getValue());
        if (selectedCardIds.size() < requiredPickCount) {
            selectedCardIds.add(cardId);
            publish(repository.observeSessionState().getValue());
        }
    }

    public void submitSelectedCard() {
        GameSessionState state = repository.observeSessionState().getValue();
        if (state == null) {
            return;
        }
        if (state.getConnectionStatus() != ConnectionStatus.CONNECTED) {
            repository.resumeSession();
            return;
        }
        if (selectedCardIds.size() == requiredPickCount(state)) {
            repository.submitCard(List.copyOf(selectedCardIds));
        }
    }

    public void leaveRoom() {
        repository.leaveRoom();
    }

    private void publish(GameSessionState sessionState) {
        GameSessionState.RoomState roomState = sessionState == null ? null : sessionState.getRoomState();
        GameSessionState.Round round = roomState == null ? null : roomState.getRound();
        GameSessionState.Viewer viewer = roomState == null ? null : roomState.getViewer();
        GameSessionState.Player localPlayer = roomState == null || viewer == null ? null : roomState.findPlayer(viewer.getPlayerId());
        int requiredPickCount = requiredPickCount(sessionState);

        pruneInvalidSelections(viewer, requiredPickCount);
        List<AnswerCardUiModel> answers = buildAnswers(viewer, localPlayer, requiredPickCount);

        Boolean demoActive = repository.observeDemoActive().getValue();
        uiState.setValue(new UiState(
                buildJudgeLabel(roomState, round),
                round == null || round.getPrompt() == null ? "Waiting for the server prompt..." : round.getPrompt().getText(),
                buildPromptMeta(sessionState, roomState, localPlayer, requiredPickCount),
                answers,
                buildHandStateMessage(sessionState, localPlayer, answers, requiredPickCount),
                buildPrimaryLabel(sessionState, viewer, localPlayer, requiredPickCount),
                isPrimaryEnabled(sessionState, viewer, requiredPickCount),
                demoActive != null && demoActive
        ));
    }

    private void pruneInvalidSelections(GameSessionState.Viewer viewer, int requiredPickCount) {
        if (viewer == null) {
            selectedCardIds.clear();
            return;
        }
        selectedCardIds.removeIf(cardId -> viewer.getHand().stream().noneMatch(card -> cardId.equals(card.getCardId())));
        while (selectedCardIds.size() > requiredPickCount) {
            selectedCardIds.remove(selectedCardIds.size() - 1);
        }
    }

    private List<AnswerCardUiModel> buildAnswers(
            GameSessionState.Viewer viewer,
            GameSessionState.Player localPlayer,
            int requiredPickCount
    ) {
        List<AnswerCardUiModel> answers = new ArrayList<>();
        if (viewer == null || localPlayer == null || localPlayer.isJudge()) {
            return answers;
        }
        for (GameSessionState.AnswerCard answerCard : viewer.getHand()) {
            int selectedIndex = selectedCardIds.indexOf(answerCard.getCardId());
            String footer;
            if (selectedIndex >= 0) {
                footer = requiredPickCount == 1
                        ? "Selected"
                        : (selectedIndex == 0 ? "First pick" : "Second pick");
            } else {
                footer = requiredPickCount == 1 ? "Tap to select" : "Tap to select in order";
            }
            answers.add(new AnswerCardUiModel(
                    answerCard.getCardId(),
                    answerCard.getText(),
                    footer,
                    selectedIndex >= 0
            ));
        }
        return answers;
    }

    private String buildJudgeLabel(GameSessionState.RoomState roomState, GameSessionState.Round round) {
        if (roomState == null || round == null) {
            return "Connecting to room...";
        }
        GameSessionState.Player judge = roomState.findPlayer(round.getJudgePlayerId());
        return judge == null ? "Judge pending" : "Judge: " + judge.getDisplayName();
    }

    private String buildPromptMeta(
            GameSessionState sessionState,
            GameSessionState.RoomState roomState,
            GameSessionState.Player localPlayer,
            int requiredPickCount
    ) {
        if (sessionState == null) {
            return "Waiting for live room state.";
        }
        if (sessionState.getErrorMessage() != null && !sessionState.getErrorMessage().isBlank()) {
            return sessionState.getErrorMessage();
        }
        if (sessionState.getStatusMessage() != null && !sessionState.getStatusMessage().isBlank()) {
            return sessionState.getStatusMessage();
        }
        if (roomState == null || roomState.getRound() == null) {
            return "The server will push the next prompt and private hand once the round is active.";
        }
        if (localPlayer != null && localPlayer.isJudge()) {
            return "You are judging this round. Answers appear once every player has locked in.";
        }
        if (localPlayer != null && localPlayer.isSubmitted()) {
            return "Your answer is locked in. Waiting for the remaining submissions.";
        }
        String progress = roomState.getRound().getReceivedSubmissions() + " of " + roomState.getRound().getRequiredSubmissions() + " submissions received.";
        if (requiredPickCount > 1) {
            return "Pick 2 blue cards in order. " + progress;
        }
        return progress;
    }

    private String buildPrimaryLabel(
            GameSessionState sessionState,
            GameSessionState.Viewer viewer,
            GameSessionState.Player localPlayer,
            int requiredPickCount
    ) {
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
        if (localPlayer != null && localPlayer.isJudge()) {
            return "Waiting for answers";
        }
        if (localPlayer != null && localPlayer.isSubmitted()) {
            return "Answer submitted";
        }
        if (viewer == null || !viewer.isCanSubmitCard()) {
            return "Waiting...";
        }
        if (selectedCardIds.size() < requiredPickCount) {
            return requiredPickCount > 1 ? "Pick 2 cards" : "Pick a card";
        }
        return "Submit answer";
    }

    private String buildHandStateMessage(
            GameSessionState sessionState,
            GameSessionState.Player localPlayer,
            List<AnswerCardUiModel> answers,
            int requiredPickCount
    ) {
        if (sessionState == null) {
            return "Waiting for a live room.";
        }
        if (sessionState.getErrorMessage() != null && !sessionState.getErrorMessage().isBlank()) {
            return sessionState.getErrorMessage();
        }
        if (sessionState.isLoading() || sessionState.getConnectionStatus() != ConnectionStatus.CONNECTED) {
            return "Refreshing your private hand from the server.";
        }
        if (localPlayer != null && localPlayer.isJudge()) {
            return "Judges sit this hand out.";
        }
        if (answers.isEmpty()) {
            return "Your hand will appear here when the next round begins.";
        }
        if (requiredPickCount > 1 && selectedCardIds.isEmpty()) {
            return "Choose 2 blue cards in the order they should be read.";
        }
        return null;
    }

    private boolean isPrimaryEnabled(GameSessionState sessionState, GameSessionState.Viewer viewer, int requiredPickCount) {
        return sessionState != null
                && sessionState.getConnectionStatus() == ConnectionStatus.CONNECTED
                && !sessionState.isLoading()
                && viewer != null
                && viewer.isCanSubmitCard()
                && selectedCardIds.size() == requiredPickCount;
    }

    private int requiredPickCount(GameSessionState sessionState) {
        if (sessionState == null || sessionState.getRoomState() == null || sessionState.getRoomState().getRound() == null) {
            return 1;
        }
        GameSessionState.Prompt prompt = sessionState.getRoomState().getRound().getPrompt();
        return prompt == null || prompt.getPickCount() < 1 ? 1 : prompt.getPickCount();
    }

    public static final class UiState {
        private final String judgeLabel;
        private final String promptText;
        private final String promptMeta;
        private final List<AnswerCardUiModel> answers;
        private final String handStateMessage;
        private final String primaryLabel;
        private final boolean primaryEnabled;
        private final boolean demoActive;

        public UiState(
                String judgeLabel,
                String promptText,
                String promptMeta,
                List<AnswerCardUiModel> answers,
                String handStateMessage,
                String primaryLabel,
                boolean primaryEnabled,
                boolean demoActive
        ) {
            this.judgeLabel = judgeLabel;
            this.promptText = promptText;
            this.promptMeta = promptMeta;
            this.answers = answers;
            this.handStateMessage = handStateMessage;
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

        public String getPromptMeta() {
            return promptMeta;
        }

        public List<AnswerCardUiModel> getAnswers() {
            return answers;
        }

        public String getHandStateMessage() {
            return handStateMessage;
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
