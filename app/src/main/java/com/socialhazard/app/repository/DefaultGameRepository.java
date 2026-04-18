package com.socialhazard.app.repository;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.socialhazard.app.model.ConnectionStatus;
import com.socialhazard.app.model.DemoScene;
import com.socialhazard.app.model.GameScreen;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.HomeContent;
import com.socialhazard.app.model.PlayerProfile;
import com.socialhazard.app.model.RoomEntryContent;
import com.socialhazard.app.model.RoomSession;
import com.socialhazard.app.model.SettingsContent;
import com.socialhazard.app.util.AvatarCatalog;
import com.socialhazard.app.network.GameSocketClient;
import com.socialhazard.app.network.SocketMessageAdapter;
import com.socialhazard.app.util.AppSettingsStore;
import com.socialhazard.app.util.Event;
import com.socialhazard.app.util.PlayerProfileStore;
import com.socialhazard.app.util.RoomSessionStore;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class DefaultGameRepository implements GameRepository, GameSocketClient.Listener {

    private static final String TAG = "GameRepository";
    private static final long RECONNECT_DELAY_MS = 2_000L;
    private static final String STATUS_CONNECTING = "Connecting to Social Hazard...";
    private static final String STATUS_RECONNECTING = "Connection dropped. Reconnecting to the room...";
    private static final String STATUS_SYNCING = "Connection restored. Syncing the latest room state...";
    private static final String STATE_SYNC_REASON_RECONNECT = "MISSED_EVENTS";

    private final GameSocketClient socketClient;
    private final SocketMessageAdapter messageAdapter;
    private final RoomSessionStore sessionStore;
    private final PlayerProfileStore profileStore;
    private final AppSettingsStore settingsStore;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<GameSessionState> sessionState;
    private final MutableLiveData<SettingsContent> settingsContent;
    private final MutableLiveData<Event<GameScreen>> navigationEvents = new MutableLiveData<>();
    private final MutableLiveData<Boolean> demoUnlocked = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> demoActive = new MutableLiveData<>(false);
    private final Deque<String> pendingMessages = new ArrayDeque<>();
    private final DemoScenarioFactory demoScenarioFactory = new DemoScenarioFactory();

    private boolean reconnectScheduled;
    private DemoScene currentDemoScene;

    public DefaultGameRepository(
            GameSocketClient socketClient,
            SocketMessageAdapter messageAdapter,
            RoomSessionStore sessionStore,
            PlayerProfileStore profileStore,
            AppSettingsStore settingsStore
    ) {
        this.socketClient = socketClient;
        this.messageAdapter = messageAdapter;
        this.sessionStore = sessionStore;
        this.profileStore = profileStore;
        this.settingsStore = settingsStore;
        this.sessionState = new MutableLiveData<>(GameSessionState.idle(sessionStore.read()));
        this.settingsContent = new MutableLiveData<>(createSettingsContent(false));
        this.socketClient.setListener(this);
    }

    @Override
    public HomeContent getHomeContent() {
        return new HomeContent(
                "ROOM-CODE",
                "Social Hazard",
                "",
                "Create a room, share the code"
        );
    }

    @Override
    public RoomEntryContent getRoomEntryContent(boolean createMode) {
        if (createMode) {
            return new RoomEntryContent(
                    true,
                    "Host a private room",
                    "Create a private room, share the four-letter code, and let the server handle the live room state.",
                    "",
                    "",
                    5,
                    false
            );
        }
        return new RoomEntryContent(
                false,
                "Join by room code",
                "Enter the four-letter code from the host, connect over WebSocket, and let the server sync the full authoritative room state.",
                "",
                "",
                5,
                false
        );
    }

    @Override
    public SettingsContent getSettingsContent() {
        SettingsContent current = settingsContent.getValue();
        return current == null ? createSettingsContent(false) : current;
    }

    @Override
    public LiveData<SettingsContent> observeSettingsContent() {
        return settingsContent;
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        PlayerProfile saved = profileStore.read();
        if (saved != null) {
            return saved;
        }
        return new PlayerProfile("", AvatarCatalog.defaultAvatarId());
    }

    @Override
    public boolean hasPlayerProfile() {
        return profileStore.read() != null;
    }

    @Override
    public void savePlayerProfile(PlayerProfile profile) {
        profileStore.save(profile);
    }

    @Override
    public void restartProfileSetup() {
        resetForFreshRoomFlow();
        profileStore.clear();
    }

    @Override
    public void setSoundEnabled(boolean enabled) {
        settingsStore.saveSoundEnabled(enabled);
        publishSettingsContent();
    }

    @Override
    public void setHapticsEnabled(boolean enabled) {
        settingsStore.saveHapticsEnabled(enabled);
        publishSettingsContent();
    }

    @Override
    public void setMotionEnabled(boolean enabled) {
        settingsStore.saveMotionEnabled(enabled);
        publishSettingsContent();
    }

    @Override
    public LiveData<GameSessionState> observeSessionState() {
        return sessionState;
    }

    @Override
    public LiveData<Event<GameScreen>> observeNavigationEvents() {
        return navigationEvents;
    }

    @Override
    public LiveData<Boolean> observeDemoUnlocked() {
        return demoUnlocked;
    }

    @Override
    public LiveData<Boolean> observeDemoActive() {
        return demoActive;
    }

    @Override
    public void resumeSession() {
        if (isDemoActive()) {
            return;
        }
        GameSessionState current = currentState();
        if (socketClient.isConnected() || socketClient.isConnecting()) {
            return;
        }
        if (!current.hasActiveRoom() && pendingMessages.isEmpty()) {
            return;
        }
        connectSocket(current.hasActiveRoom() ? ConnectionStatus.RECONNECTING : ConnectionStatus.CONNECTING);
    }

    @Override
    public void createRoom(String displayName, String avatarId, int targetScore) {
        resetForFreshRoomFlow();
        queueMessage(
                messageAdapter.createCreateRoom(displayName, avatarId, targetScore),
                ConnectionStatus.CONNECTING,
                "Creating room..."
        );
    }

    @Override
    public void joinRoom(String roomCode, String displayName, String avatarId) {
        resetForFreshRoomFlow();
        queueMessage(
                messageAdapter.createJoinRoom(roomCode, displayName, avatarId),
                ConnectionStatus.CONNECTING,
                "Joining room..."
        );
    }

    @Override
    public void toggleReady() {
        if (isDemoActive()) {
            toggleDemoReady();
            return;
        }
        GameSessionState.RoomState roomState = requireRoomState();
        RoomSession roomSession = requireRoomSession();
        GameSessionState.Player player = roomState.findPlayer(roomState.getViewer().getPlayerId());
        boolean nextReady = player == null || !player.isReady();
        sendAuthenticated(
                messageAdapter.createToggleReady(roomSession, nextReady),
                "Updating ready state..."
        );
    }

    @Override
    public void startGame() {
        if (isDemoActive()) {
            showDemoScene(DemoScene.SUBMITTING);
            return;
        }
        sendAuthenticated(messageAdapter.createStartGame(requireRoomSession()), "Starting match...");
    }

    @Override
    public void submitCard(List<String> cardIds) {
        if (isDemoActive()) {
            submitDemoCard();
            return;
        }
        GameSessionState.RoomState roomState = requireRoomState();
        GameSessionState.Round round = roomState.getRound();
        if (round == null || cardIds == null || cardIds.isEmpty()) {
            return;
        }
        sendAuthenticated(
                messageAdapter.createSubmitCard(requireRoomSession(), round.getRoundId(), cardIds),
                "Submitting answer..."
        );
    }

    @Override
    public void judgePick(String submissionId) {
        if (isDemoActive()) {
            judgeDemoSubmission(submissionId);
            return;
        }
        GameSessionState.RoomState roomState = requireRoomState();
        GameSessionState.Round round = roomState.getRound();
        if (round == null) {
            return;
        }
        sendAuthenticated(
                messageAdapter.createJudgePick(requireRoomSession(), round.getRoundId(), submissionId),
                "Locking in the winning card..."
        );
    }

    @Override
    public void leaveRoom() {
        if (isDemoActive()) {
            deactivateDemoMode();
            navigationEvents.setValue(new Event<>(GameScreen.HOME));
            return;
        }
        RoomSession roomSession = currentState().getRoomSession();
        if (roomSession == null) {
            clearSessionAndNavigateHome(null);
            return;
        }
        if (!socketClient.isConnected()) {
            clearSessionAndNavigateHome(null);
            return;
        }
        sendAuthenticated(messageAdapter.createLeaveRoom(roomSession), "Leaving room...");
    }

    @Override
    public void dismissRoundReveal() {
        if (isDemoActive() && currentDemoScene == DemoScene.ROUND_RESULT) {
            showDemoScene(DemoScene.JUDGING);
            return;
        }
        GameSessionState current = currentState();
        if (current.getRoundReveal() == null) {
            return;
        }
        postSessionState(current.buildUpon().setRoundReveal(null).build());
        emitNavigationForCurrentRoom();
    }

    @Override
    public void startSoloDemo() {
        showDemoScene(DemoScene.LOBBY);
    }

    @Override
    public void showDemoScene(DemoScene scene) {
        activateDemoMode();
        currentDemoScene = scene;
        switch (scene) {
            case LOBBY:
                postSessionState(demoScenarioFactory.lobbyState(false));
                navigationEvents.setValue(new Event<>(GameScreen.LOBBY));
                break;
            case SUBMITTING:
                postSessionState(demoScenarioFactory.submittingState(false));
                navigationEvents.setValue(new Event<>(GameScreen.GAME));
                break;
            case JUDGING:
                postSessionState(demoScenarioFactory.judgingState());
                navigationEvents.setValue(new Event<>(GameScreen.JUDGE));
                break;
            case ROUND_RESULT:
                postSessionState(demoScenarioFactory.roundResultState());
                navigationEvents.setValue(new Event<>(GameScreen.ROUND_RESULT));
                break;
            case FINAL_SCOREBOARD:
                postSessionState(demoScenarioFactory.finalScoreboardState(DemoScenarioFactory.SUBMISSION_BETA));
                navigationEvents.setValue(new Event<>(GameScreen.FINAL_SCOREBOARD));
                break;
            default:
                break;
        }
    }

    @Override
    public void unlockDemoMode() {
        demoUnlocked.setValue(true);
        publishSettingsContent();
    }

    @Override
    public void resetDemoMode() {
        demoUnlocked.setValue(false);
        publishSettingsContent();
        if (isDemoActive()) {
            deactivateDemoMode();
        }
    }

    @Override
    public void onConnected() {
        if (isDemoActive()) {
            return;
        }
        Log.i(TAG, "Socket connected.");
        reconnectScheduled = false;
        GameSessionState current = currentState();
        RoomSession savedSession = current.getRoomSession();
        if (savedSession != null) {
            long lastKnownStateVersion = current.getRoomState() == null ? 0L : current.getRoomState().getStateVersion();
            Log.i(TAG, "Reconnected socket. Requesting room restore for roomCode=" + savedSession.getRoomCode()
                    + " stateVersion=" + lastKnownStateVersion);
            postSessionState(current.buildUpon()
                    .setConnectionStatus(ConnectionStatus.CONNECTED)
                    .setLoading(true)
                    .setStatusMessage("Restoring room session...")
                    .setErrorMessage(null)
                    .build());
            socketClient.send(messageAdapter.createReconnect(savedSession, lastKnownStateVersion));
            return;
        }

        postSessionState(current.buildUpon()
                .setConnectionStatus(ConnectionStatus.CONNECTED)
                .setStatusMessage(null)
                .setErrorMessage(null)
                .build());
        flushPendingMessages();
    }

    @Override
    public void onMessage(String text) {
        if (isDemoActive()) {
            return;
        }
        SocketMessageAdapter.ParsedServerMessage message = messageAdapter.parse(text);
        switch (message.getType()) {
            case "RoomCreated":
            case "RoomJoined":
                handleRoomConnection(message.getRoomConnection(), false);
                break;
            case "ReconnectedState":
                handleRoomConnection(message.getRoomConnection(), true);
                break;
            case "RoomStateUpdated":
            case "GameStarted":
            case "PromptShown":
            case "HandUpdated":
            case "SubmissionAccepted":
            case "JudgePhaseStarted":
            case "RoundAdvanced":
                applyRoomState(message.getRoomState(), false);
                break;
            case "WinnerChosen":
                applyRoomState(message.getRoomState(), true);
                break;
            case "GameOver":
                applyGameOver(message.getRoomState());
                break;
            case "ErrorMessage":
                handleError(message.getErrorCode(), message.getErrorMessage());
                break;
            case "RoomLeft":
                clearSessionAndNavigateHome(null);
                break;
            case "SessionReplaced":
                clearSessionAndNavigateHome(message.getReason());
                break;
            default:
                break;
        }
    }

    @Override
    public void onDisconnected(String reason) {
        if (isDemoActive()) {
            return;
        }
        Log.w(TAG, "Socket disconnected. reason=" + reason);
        handleSocketDisconnected(reason);
    }

    @Override
    public void onFailure(String message) {
        if (isDemoActive()) {
            return;
        }
        Log.e(TAG, "Socket failure. message=" + message);
        handleSocketDisconnected(message);
    }

    private void handleRoomConnection(SocketMessageAdapter.RoomConnection connection, boolean requestStateSync) {
        RoomSession roomSession = connection.getRoomSession();
        GameSessionState.RoomState roomState = connection.getRoomState();
        RoomSession savedSession = new RoomSession(
                roomSession.getRoomCode(),
                roomSession.getPlayerId(),
                roomSession.getPlayerToken(),
                displayNameFor(roomState, roomSession.getPlayerId()),
                avatarIdFor(roomState, roomSession.getPlayerId())
        );
        sessionStore.save(savedSession);

        postSessionState(currentState().buildUpon()
                .setConnectionStatus(ConnectionStatus.CONNECTED)
                .setLoading(false)
                .setStatusMessage(null)
                .setErrorMessage(null)
                .setRoomSession(savedSession)
                .setRoomState(roomState)
                .setRoundReveal(null)
                .build());
        emitNavigationForCurrentRoom();

        if (requestStateSync && roomState != null) {
            if (!socketClient.send(messageAdapter.createRequestStateSync(
                    savedSession,
                    roomState.getStateVersion(),
                    STATE_SYNC_REASON_RECONNECT
            ))) {
                handleSocketDisconnected("Unable to sync the room after reconnecting.");
                return;
            }
            postSessionState(currentState().buildUpon()
                    .setLoading(true)
                    .setStatusMessage(STATUS_SYNCING)
                    .build());
        } else {
            flushPendingMessages();
        }
    }

    private void applyRoomState(GameSessionState.RoomState roomState, boolean createRoundReveal) {
        if (roomState == null) {
            return;
        }
        GameSessionState current = currentState();
        GameSessionState.RoomState currentRoomState = current.getRoomState();
        if (currentRoomState != null
                && currentRoomState.getStateVersion() > 0L
                && roomState.getStateVersion() > 0L
                && roomState.getStateVersion() < currentRoomState.getStateVersion()) {
            return;
        }
        GameSessionState.RoundReveal roundReveal = current.getRoundReveal();
        if (createRoundReveal) {
            roundReveal = buildRoundReveal(roomState);
        }

        postSessionState(current.buildUpon()
                .setConnectionStatus(ConnectionStatus.CONNECTED)
                .setLoading(false)
                .setStatusMessage(null)
                .setErrorMessage(null)
                .setRoomState(roomState)
                .setRoundReveal(roundReveal)
                .build());

        if (socketClient.isConnected() && !pendingMessages.isEmpty()) {
            flushPendingMessages();
        }

        if (createRoundReveal) {
            navigationEvents.setValue(new Event<>(GameScreen.ROUND_RESULT));
            return;
        }
        if (roundReveal == null) {
            emitNavigationForCurrentRoom();
        }
    }

    private void applyGameOver(GameSessionState.RoomState roomState) {
        postSessionState(currentState().buildUpon()
                .setConnectionStatus(ConnectionStatus.CONNECTED)
                .setLoading(false)
                .setStatusMessage(null)
                .setErrorMessage(null)
                .setRoomState(roomState)
                .setRoundReveal(null)
                .build());
        navigationEvents.setValue(new Event<>(GameScreen.FINAL_SCOREBOARD));
    }

    private void handleError(String code, String message) {
        GameSessionState current = currentState();
        pendingMessages.clear();
        if (current.hasActiveRoom() && isTerminalSessionError(code)) {
            clearSessionAndNavigateHome(message);
            return;
        }
        postSessionState(current.buildUpon()
                .setConnectionStatus(current.hasActiveRoom() ? current.getConnectionStatus() : ConnectionStatus.ERROR)
                .setLoading(false)
                .setStatusMessage(null)
                .setErrorMessage(message)
                .build());
    }

    private void handleSocketDisconnected(String reason) {
        GameSessionState current = currentState();
        String failureMessage = normalizeFailureMessage(reason);
        if (!current.hasActiveRoom()) {
            Log.w(TAG, "Socket unavailable before room session was established. message=" + failureMessage);
            postSessionState(current.buildUpon()
                    .setConnectionStatus(ConnectionStatus.ERROR)
                    .setLoading(false)
                    .setStatusMessage(null)
                    .setErrorMessage(failureMessage)
                    .build());
            return;
        }

        postSessionState(current.buildUpon()
                .setConnectionStatus(ConnectionStatus.RECONNECTING)
                .setLoading(true)
                .setStatusMessage(STATUS_RECONNECTING)
                .setErrorMessage(null)
                .build());

        if (reconnectScheduled) {
            Log.i(TAG, "Reconnect already scheduled. Skipping duplicate schedule.");
            return;
        }
        reconnectScheduled = true;
        RoomSession roomSession = current.getRoomSession();
        String roomCode = roomSession == null ? "unknown" : roomSession.getRoomCode();
        Log.i(TAG, "Scheduling reconnect for roomCode=" + roomCode + " in " + RECONNECT_DELAY_MS + "ms");
        mainHandler.postDelayed(() -> {
            reconnectScheduled = false;
            if (!socketClient.isConnected() && !socketClient.isConnecting() && currentState().hasActiveRoom()) {
                RoomSession latestSession = currentState().getRoomSession();
                String latestRoomCode = latestSession == null ? "unknown" : latestSession.getRoomCode();
                Log.i(TAG, "Attempting reconnect for roomCode=" + latestRoomCode);
                socketClient.connect();
            }
        }, RECONNECT_DELAY_MS);
    }

    private void queueMessage(String message, ConnectionStatus connectionStatus, String statusMessage) {
        pendingMessages.add(message);
        postSessionState(currentState().buildUpon()
                .setConnectionStatus(connectionStatus)
                .setLoading(true)
                .setStatusMessage(statusMessage)
                .setErrorMessage(null)
                .build());
        if (socketClient.isConnected()) {
            flushPendingMessages();
            return;
        }
        if (!socketClient.isConnecting()) {
            connectSocket(connectionStatus);
        }
    }

    private void sendAuthenticated(String message, String statusMessage) {
        pendingMessages.add(message);
        postSessionState(currentState().buildUpon()
                .setLoading(true)
                .setStatusMessage(statusMessage)
                .setErrorMessage(null)
                .build());
        if (socketClient.isConnected()) {
            flushPendingMessages();
        } else if (!socketClient.isConnecting()) {
            connectSocket(ConnectionStatus.RECONNECTING);
        }
    }

    private void connectSocket(ConnectionStatus connectionStatus) {
        postSessionState(currentState().buildUpon()
                .setConnectionStatus(connectionStatus)
                .setLoading(true)
                .setStatusMessage(connectionStatus == ConnectionStatus.RECONNECTING ? STATUS_RECONNECTING : STATUS_CONNECTING)
                .setErrorMessage(null)
                .build());
        socketClient.connect();
    }

    private void toggleDemoReady() {
        GameSessionState current = currentState();
        GameSessionState.RoomState roomState = current.getRoomState();
        if (roomState == null || roomState.getPhase() != GameSessionState.Phase.LOBBY) {
            return;
        }
        GameSessionState.Player localPlayer = roomState.findPlayer(DemoScenarioFactory.LOCAL_PLAYER_ID);
        boolean nextReady = localPlayer == null || !localPlayer.isReady();
        postSessionState(demoScenarioFactory.lobbyState(nextReady));
    }

    private void submitDemoCard() {
        if (currentDemoScene != DemoScene.SUBMITTING) {
            return;
        }
        postSessionState(demoScenarioFactory.submittingState(true).buildUpon()
                .setLoading(true)
                .setStatusMessage("Bot players are locking in the rest of the demo round...")
                .build());
        mainHandler.removeCallbacksAndMessages(null);
        mainHandler.postDelayed(() -> showDemoScene(DemoScene.ROUND_RESULT), 700L);
    }

    private void judgeDemoSubmission(String submissionId) {
        if (currentDemoScene != DemoScene.JUDGING || submissionId == null || submissionId.isBlank()) {
            return;
        }
        postSessionState(currentState().buildUpon()
                .setLoading(true)
                .setStatusMessage("Finalizing the offline demo scoreboard...")
                .build());
        mainHandler.removeCallbacksAndMessages(null);
        mainHandler.postDelayed(() -> {
            activateDemoMode();
            currentDemoScene = DemoScene.FINAL_SCOREBOARD;
            postSessionState(demoScenarioFactory.finalScoreboardState(submissionId));
            navigationEvents.setValue(new Event<>(GameScreen.FINAL_SCOREBOARD));
        }, 650L);
    }

    private void activateDemoMode() {
        mainHandler.removeCallbacksAndMessages(null);
        reconnectScheduled = false;
        pendingMessages.clear();
        sessionStore.clear();
        socketClient.disconnect();
        demoActive.setValue(true);
    }

    private void deactivateDemoMode() {
        mainHandler.removeCallbacksAndMessages(null);
        reconnectScheduled = false;
        pendingMessages.clear();
        currentDemoScene = null;
        demoActive.setValue(false);
        postSessionState(new GameSessionState(
                ConnectionStatus.DISCONNECTED,
                false,
                null,
                null,
                null,
                null,
                null
        ));
    }

    private void resetForFreshRoomFlow() {
        mainHandler.removeCallbacksAndMessages(null);
        reconnectScheduled = false;
        pendingMessages.clear();
        currentDemoScene = null;
        demoActive.setValue(false);
        sessionStore.clear();
        socketClient.disconnect();
        postSessionState(new GameSessionState(
                ConnectionStatus.DISCONNECTED,
                false,
                null,
                null,
                null,
                null,
                null
        ));
    }

    private void flushPendingMessages() {
        while (!pendingMessages.isEmpty()) {
            String message = pendingMessages.pollFirst();
            if (message == null) {
                continue;
            }
            if (!socketClient.send(message)) {
                pendingMessages.addFirst(message);
                handleSocketDisconnected("Unable to send the latest room event.");
                return;
            }
        }
    }

    private void clearSessionAndNavigateHome(String errorMessage) {
        mainHandler.removeCallbacksAndMessages(null);
        reconnectScheduled = false;
        pendingMessages.clear();
        currentDemoScene = null;
        demoActive.setValue(false);
        sessionStore.clear();
        socketClient.disconnect();
        postSessionState(new GameSessionState(
                ConnectionStatus.DISCONNECTED,
                false,
                null,
                errorMessage,
                null,
                null,
                null
        ));
        navigationEvents.setValue(new Event<>(GameScreen.HOME));
    }

    private String normalizeFailureMessage(String reason) {
        if (reason == null || reason.isBlank()) {
            return "Unable to reach the backend right now.";
        }
        return reason;
    }

    private void emitNavigationForCurrentRoom() {
        GameSessionState current = currentState();
        if (current.getRoundReveal() != null) {
            navigationEvents.setValue(new Event<>(GameScreen.ROUND_RESULT));
            return;
        }

        GameSessionState.RoomState roomState = current.getRoomState();
        if (roomState == null) {
            return;
        }

        GameScreen screen;
        switch (roomState.getPhase()) {
            case LOBBY:
                screen = GameScreen.LOBBY;
                break;
            case SUBMITTING:
                screen = GameScreen.GAME;
                break;
            case JUDGING:
                screen = GameScreen.JUDGE;
                break;
            case ROUND_RESULT:
                screen = GameScreen.ROUND_RESULT;
                break;
            case GAME_OVER:
                screen = GameScreen.FINAL_SCOREBOARD;
                break;
            default:
                screen = GameScreen.LOBBY;
                break;
        }
        navigationEvents.setValue(new Event<>(screen));
    }

    private GameSessionState.RoundReveal buildRoundReveal(GameSessionState.RoomState roomState) {
        if (roomState == null || roomState.getRound() == null) {
            return null;
        }
        GameSessionState.Round round = roomState.getRound();
        String winnerPlayerId = round.getWinnerPlayerId();
        GameSessionState.Player winner = roomState.findPlayer(winnerPlayerId);
        String winnerName = winner == null ? "Round winner" : winner.getDisplayName();
        String winningAnswer = "";
        for (GameSessionState.Submission submission : round.getSubmissions()) {
            if (submission.isWinner() || round.getWinningSubmissionId().equals(submission.getSubmissionId())) {
                winningAnswer = submission.getText();
                break;
            }
        }
        return new GameSessionState.RoundReveal(
                round.getRoundNumber(),
                winnerName,
                winningAnswer,
                roomState.getScoreboard()
        );
    }

    private boolean isTerminalSessionError(String code) {
        return "ROOM_NOT_FOUND".equals(code)
                || "PLAYER_NOT_FOUND".equals(code)
                || "UNAUTHORIZED".equals(code)
                || "SESSION_NOT_ATTACHED".equals(code);
    }

    private GameSessionState.RoomState requireRoomState() {
        GameSessionState.RoomState roomState = currentState().getRoomState();
        if (roomState == null) {
            throw new IllegalStateException("No active room state available.");
        }
        return roomState;
    }

    private RoomSession requireRoomSession() {
        RoomSession roomSession = currentState().getRoomSession();
        if (roomSession == null) {
            throw new IllegalStateException("No active room session available.");
        }
        return roomSession;
    }

    private String displayNameFor(GameSessionState.RoomState roomState, String playerId) {
        if (roomState == null) {
            return "";
        }
        GameSessionState.Player player = roomState.findPlayer(playerId);
        return player == null ? "" : player.getDisplayName();
    }

    private String avatarIdFor(GameSessionState.RoomState roomState, String playerId) {
        if (roomState == null) {
            return AvatarCatalog.defaultAvatarId();
        }
        GameSessionState.Player player = roomState.findPlayer(playerId);
        return player == null ? AvatarCatalog.defaultAvatarId() : player.getAvatarId();
    }

    private GameSessionState currentState() {
        GameSessionState current = sessionState.getValue();
        return current == null ? GameSessionState.idle(sessionStore.read()) : current;
    }

    private void postSessionState(GameSessionState nextState) {
        sessionState.setValue(nextState);
    }

    private void publishSettingsContent() {
        Boolean unlocked = demoUnlocked.getValue();
        settingsContent.setValue(createSettingsContent(unlocked != null && unlocked));
    }

    private SettingsContent createSettingsContent(boolean demoUnlockedValue) {
        return new SettingsContent(
                settingsStore.readSoundEnabled(),
                settingsStore.readHapticsEnabled(),
                settingsStore.readMotionEnabled(),
                demoUnlockedValue
        );
    }

    private boolean isDemoActive() {
        Boolean active = demoActive.getValue();
        return active != null && active;
    }
}
