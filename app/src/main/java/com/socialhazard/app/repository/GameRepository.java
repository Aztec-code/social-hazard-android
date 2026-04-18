package com.socialhazard.app.repository;

import androidx.lifecycle.LiveData;

import com.socialhazard.app.model.DemoScene;
import com.socialhazard.app.model.GameScreen;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.model.HomeContent;
import com.socialhazard.app.model.PlayerProfile;
import com.socialhazard.app.model.RoomEntryContent;
import com.socialhazard.app.model.SettingsContent;
import com.socialhazard.app.util.Event;

import java.util.List;

public interface GameRepository {

    HomeContent getHomeContent();

    RoomEntryContent getRoomEntryContent(boolean createMode);

    SettingsContent getSettingsContent();

    LiveData<SettingsContent> observeSettingsContent();

    PlayerProfile getPlayerProfile();

    boolean hasPlayerProfile();

    void savePlayerProfile(PlayerProfile profile);

    void restartProfileSetup();

    void setSoundEnabled(boolean enabled);

    void setHapticsEnabled(boolean enabled);

    void setMotionEnabled(boolean enabled);

    LiveData<GameSessionState> observeSessionState();

    LiveData<Event<GameScreen>> observeNavigationEvents();

    LiveData<Boolean> observeDemoUnlocked();

    LiveData<Boolean> observeDemoActive();

    void resumeSession();

    void createRoom(String displayName, String avatarId, int targetScore);

    void joinRoom(String roomCode, String displayName, String avatarId);

    void toggleReady();

    void startGame();

    void submitCard(List<String> cardIds);

    void judgePick(String submissionId);

    void leaveRoom();

    void dismissRoundReveal();

    void startSoloDemo();

    void showDemoScene(DemoScene scene);

    void unlockDemoMode();

    void resetDemoMode();
}
