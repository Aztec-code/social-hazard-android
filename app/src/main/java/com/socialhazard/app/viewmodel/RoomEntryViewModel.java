package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.socialhazard.app.model.ConnectionStatus;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.PlayerProfile;
import com.socialhazard.app.model.RoomEntryContent;
import com.socialhazard.app.repository.GameRepository;

public final class RoomEntryViewModel extends BaseViewModel {

    private final GameRepository repository;
    private final MediatorLiveData<UiState> uiState = new MediatorLiveData<>();

    private boolean createMode = true;
    private int targetScore = 5;
    private String roomCode = "";
    private String roomCodeError;

    public RoomEntryViewModel(GameRepository repository) {
        this.repository = repository;
        uiState.addSource(repository.observeSessionState(), this::publish);
        seed(true);
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void seed(boolean createMode) {
        RoomEntryContent content = repository.getRoomEntryContent(createMode);
        this.createMode = content.isCreateMode();
        this.targetScore = content.getTargetScore();
        this.roomCode = content.getRoomCode();
        this.roomCodeError = null;
        publish(repository.observeSessionState().getValue());
    }

    public void setCreateMode(boolean createMode) {
        if (this.createMode == createMode) {
            return;
        }
        seed(createMode);
    }

    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
        publish(repository.observeSessionState().getValue());
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode == null ? "" : roomCode.trim().toUpperCase();
        this.roomCodeError = null;
        publish(repository.observeSessionState().getValue());
    }

    public void submit() {
        roomCodeError = null;

        PlayerProfile profile = repository.getPlayerProfile();
        if (!createMode && roomCode.length() != 4) {
            roomCodeError = "Room codes are 4 letters.";
        }
        if (!profile.isComplete() || roomCodeError != null) {
            publish(repository.observeSessionState().getValue());
            return;
        }

        if (createMode) {
            repository.createRoom(profile.getNickname(), profile.getAvatarId(), targetScore);
        } else {
            repository.joinRoom(roomCode, profile.getNickname(), profile.getAvatarId());
        }
    }

    private void publish(GameSessionState sessionState) {
        RoomEntryContent content = repository.getRoomEntryContent(createMode);
        PlayerProfile profile = repository.getPlayerProfile();
        boolean loading = sessionState != null && sessionState.isLoading() && sessionState.getRoomState() == null;
        String statusLine = buildStatusLine(sessionState);

        uiState.setValue(new UiState(
                createMode,
                content.getHeadline(),
                mergeBody(content.getBody(), statusLine, profile),
                roomCode,
                targetScore,
                buildPrimaryLabel(sessionState, loading),
                !loading,
                roomCodeError,
                loading,
                sessionState != null && sessionState.getConnectionStatus() == ConnectionStatus.RECONNECTING
        ));
    }

    private String buildStatusLine(GameSessionState sessionState) {
        if (sessionState == null) {
            return null;
        }
        if (sessionState.getErrorMessage() != null && !sessionState.getErrorMessage().isBlank()) {
            return sessionState.getErrorMessage();
        }
        if (sessionState.getStatusMessage() != null && !sessionState.getStatusMessage().isBlank()) {
            return sessionState.getStatusMessage();
        }
        return null;
    }

    private String mergeBody(String baseBody, String statusLine, PlayerProfile profile) {
        String profileLine = profile.isComplete() ? "Playing as " + profile.getNickname() : null;
        if ((statusLine == null || statusLine.isBlank()) && profileLine == null) {
            return baseBody;
        }
        StringBuilder builder = new StringBuilder(baseBody);
        if (profileLine != null) {
            builder.append("\n\n").append(profileLine);
        }
        if (statusLine != null && !statusLine.isBlank()) {
            builder.append("\n\n").append(statusLine);
        }
        return builder.toString();
    }

    private String buildPrimaryLabel(GameSessionState sessionState, boolean loading) {
        if (loading) {
            return "Connecting...";
        }
        if (sessionState != null && sessionState.getConnectionStatus() == ConnectionStatus.ERROR) {
            return "Retry connection";
        }
        return createMode ? "Create room" : "Join room";
    }

    public static final class UiState {
        private final boolean createMode;
        private final String headline;
        private final String body;
        private final String roomCode;
        private final int targetScore;
        private final String primaryLabel;
        private final boolean primaryEnabled;
        private final String roomCodeError;
        private final boolean loading;
        private final boolean reconnecting;

        public UiState(
                boolean createMode,
                String headline,
                String body,
                String roomCode,
                int targetScore,
                String primaryLabel,
                boolean primaryEnabled,
                String roomCodeError,
                boolean loading,
                boolean reconnecting
        ) {
            this.createMode = createMode;
            this.headline = headline;
            this.body = body;
            this.roomCode = roomCode;
            this.targetScore = targetScore;
            this.primaryLabel = primaryLabel;
            this.primaryEnabled = primaryEnabled;
            this.roomCodeError = roomCodeError;
            this.loading = loading;
            this.reconnecting = reconnecting;
        }

        public boolean isCreateMode() {
            return createMode;
        }

        public String getHeadline() {
            return headline;
        }

        public String getBody() {
            return body;
        }

        public String getRoomCode() {
            return roomCode;
        }

        public int getTargetScore() {
            return targetScore;
        }

        public String getPrimaryLabel() {
            return primaryLabel;
        }

        public boolean isPrimaryEnabled() {
            return primaryEnabled;
        }

        public String getRoomCodeError() {
            return roomCodeError;
        }

        public boolean isLoading() {
            return loading;
        }

        public boolean isReconnecting() {
            return reconnecting;
        }
    }
}
