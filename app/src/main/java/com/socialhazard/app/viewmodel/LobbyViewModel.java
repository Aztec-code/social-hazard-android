package com.socialhazard.app.viewmodel;

import android.graphics.Color;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.socialhazard.app.model.ConnectionStatus;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.PlayerSlotUiModel;
import com.socialhazard.app.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

public final class LobbyViewModel extends BaseViewModel {

    private static final int[] ACCENT_COLORS = new int[]{
            Color.parseColor("#F0A33E"),
            Color.parseColor("#53C8BE"),
            Color.parseColor("#7AA2F7"),
            Color.parseColor("#E873A8")
    };

    private final GameRepository repository;
    private final MediatorLiveData<UiState> uiState = new MediatorLiveData<>();

    public LobbyViewModel(GameRepository repository) {
        this.repository = repository;
        uiState.addSource(repository.observeSessionState(), this::publish);
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void onPrimaryAction() {
        GameSessionState state = repository.observeSessionState().getValue();
        if (state == null) {
            return;
        }
        if (state.getConnectionStatus() != ConnectionStatus.CONNECTED) {
            repository.resumeSession();
            return;
        }
        GameSessionState.RoomState roomState = state.getRoomState();
        if (roomState == null || roomState.getViewer() == null) {
            return;
        }
        if (roomState.getViewer().isCanStartGame()) {
            repository.startGame();
            return;
        }
        if (roomState.getViewer().isCanToggleReady()) {
            repository.toggleReady();
        }
    }

    public void leaveRoom() {
        repository.leaveRoom();
    }

    private void publish(GameSessionState sessionState) {
        GameSessionState.RoomState roomState = sessionState == null ? null : sessionState.getRoomState();
        GameSessionState.Viewer viewer = roomState == null ? null : roomState.getViewer();
        GameSessionState.Player localPlayer = roomState == null || viewer == null ? null : roomState.findPlayer(viewer.getPlayerId());

        String roomCode = roomState == null ? "----" : roomState.getRoomCode();
        String modeLabel = "Classic mode";
        String scoreLabel = roomState == null ? "Target score" : "Target score " + roomState.getTargetScore();
        String populationLabel = roomState == null
                ? "0 / 4"
                : roomState.getPlayerCount() + " / 4";
        String readinessLabel = buildReadinessLabel(roomState);
        String body = buildBody(sessionState, roomState, localPlayer);
        String primaryLabel = buildPrimaryLabel(sessionState, viewer, localPlayer);
        boolean primaryEnabled = sessionState != null
                && sessionState.getConnectionStatus() == ConnectionStatus.CONNECTED
                && !sessionState.isLoading()
                && viewer != null
                && (viewer.isCanStartGame() || viewer.isCanToggleReady());

        uiState.setValue(new UiState(
                roomCode,
                modeLabel,
                scoreLabel,
                populationLabel,
                readinessLabel,
                body,
                buildPlayers(roomState),
                buildPlayerStateMessage(sessionState, roomState),
                primaryLabel,
                primaryEnabled
        ));
    }

    private String buildBody(GameSessionState sessionState, GameSessionState.RoomState roomState, GameSessionState.Player localPlayer) {
        if (sessionState == null) {
            return "Waiting for the app session.";
        }
        if (sessionState.getErrorMessage() != null && !sessionState.getErrorMessage().isBlank()) {
            return sessionState.getErrorMessage();
        }
        if (sessionState.getStatusMessage() != null && !sessionState.getStatusMessage().isBlank()) {
            return sessionState.getStatusMessage();
        }
        if (roomState == null) {
            return "Create or join a room to populate the live lobby.";
        }
        if (localPlayer != null && localPlayer.isReady()) {
            return "You are ready. The server will start once the host begins the match.";
        }
        if (localPlayer != null && roomState.getViewer() != null && roomState.getViewer().isCanStartGame()) {
            return "Everyone is connected and ready. Start the match when the room looks good.";
        }
        return "The server owns readiness, seat order, and room authority. This lobby simply renders the current room state.";
    }

    private String buildPrimaryLabel(
            GameSessionState sessionState,
            GameSessionState.Viewer viewer,
            GameSessionState.Player localPlayer
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
        if (viewer == null) {
            return "Waiting...";
        }
        if (viewer.isCanStartGame()) {
            return "Start match";
        }
        if (localPlayer != null && localPlayer.isReady()) {
            return "Unready";
        }
        return "Ready up";
    }

    private String buildReadinessLabel(GameSessionState.RoomState roomState) {
        if (roomState == null) {
            return "Waiting";
        }
        int readyCount = 0;
        for (GameSessionState.Player player : roomState.getPlayers()) {
            if (player.isReady()) {
                readyCount++;
            }
        }
        if (readyCount == roomState.getPlayerCount() && roomState.getPlayerCount() > 0) {
            return "All set";
        }
        return readyCount + " ready";
    }

    private String buildPlayerStateMessage(GameSessionState sessionState, GameSessionState.RoomState roomState) {
        if (sessionState == null) {
            return "Waiting for a room session.";
        }
        if (sessionState.isLoading() || sessionState.getConnectionStatus() != ConnectionStatus.CONNECTED) {
            return "Room roster is syncing with the server.";
        }
        if (roomState == null || roomState.getPlayers().isEmpty()) {
            return "Players will appear here once the room is live.";
        }
        return null;
    }

    private List<PlayerSlotUiModel> buildPlayers(GameSessionState.RoomState roomState) {
        List<PlayerSlotUiModel> players = new ArrayList<>();
        if (roomState == null) {
            return players;
        }
        for (GameSessionState.Player player : roomState.getPlayers()) {
            String subtitle = player.isConnected() ? (player.isReady() ? "Connected / Ready" : "Connected / Not ready") : "Disconnected";
            if (player.isJudge()) {
                subtitle = subtitle + " / Judge";
            }
            players.add(new PlayerSlotUiModel(
                    player.getDisplayName(),
                    player.getAvatarId(),
                    subtitle,
                    "Seat " + (char) ('A' + player.getSeatIndex()),
                    true,
                    player.isHost(),
                    ACCENT_COLORS[player.getSeatIndex() % ACCENT_COLORS.length]
            ));
        }
        return players;
    }

    public static final class UiState {
        private final String roomCode;
        private final String modeLabel;
        private final String targetScoreLabel;
        private final String populationLabel;
        private final String readinessLabel;
        private final String body;
        private final List<PlayerSlotUiModel> players;
        private final String playerStateMessage;
        private final String primaryLabel;
        private final boolean primaryEnabled;

        public UiState(
                String roomCode,
                String modeLabel,
                String targetScoreLabel,
                String populationLabel,
                String readinessLabel,
                String body,
                List<PlayerSlotUiModel> players,
                String playerStateMessage,
                String primaryLabel,
                boolean primaryEnabled
        ) {
            this.roomCode = roomCode;
            this.modeLabel = modeLabel;
            this.targetScoreLabel = targetScoreLabel;
            this.populationLabel = populationLabel;
            this.readinessLabel = readinessLabel;
            this.body = body;
            this.players = players;
            this.playerStateMessage = playerStateMessage;
            this.primaryLabel = primaryLabel;
            this.primaryEnabled = primaryEnabled;
        }

        public String getRoomCode() {
            return roomCode;
        }

        public String getModeLabel() {
            return modeLabel;
        }

        public String getTargetScoreLabel() {
            return targetScoreLabel;
        }

        public String getPopulationLabel() {
            return populationLabel;
        }

        public String getReadinessLabel() {
            return readinessLabel;
        }

        public String getBody() {
            return body;
        }

        public List<PlayerSlotUiModel> getPlayers() {
            return players;
        }

        public String getPlayerStateMessage() {
            return playerStateMessage;
        }

        public String getPrimaryLabel() {
            return primaryLabel;
        }

        public boolean isPrimaryEnabled() {
            return primaryEnabled;
        }
    }
}
