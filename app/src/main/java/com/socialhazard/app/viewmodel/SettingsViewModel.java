package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.socialhazard.app.model.SettingsContent;
import com.socialhazard.app.repository.GameRepository;
import com.socialhazard.app.util.Event;

public final class SettingsViewModel extends BaseViewModel {

    private final GameRepository repository;
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> restartProfileEvents = new MutableLiveData<>();
    private int tapsRemaining = 5;

    public SettingsViewModel(GameRepository repository) {
        this.repository = repository;
        publish();
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public LiveData<Event<Boolean>> getRestartProfileEvents() {
        return restartProfileEvents;
    }

    public void setSoundEnabled(boolean enabled) {
        repository.setSoundEnabled(enabled);
        publish();
    }

    public void setHapticsEnabled(boolean enabled) {
        repository.setHapticsEnabled(enabled);
        publish();
    }

    public void setMotionEnabled(boolean enabled) {
        repository.setMotionEnabled(enabled);
        publish();
    }

    public void onVersionTapped() {
        UiState current = uiState.getValue();
        if (current == null) {
            return;
        }
        if (current.isDemoUnlocked()) {
            postMessage("Demo mode already unlocked.");
            return;
        }
        tapsRemaining--;
        if (tapsRemaining <= 0) {
            repository.unlockDemoMode();
            publish();
            postMessage("Hidden demo mode unlocked.");
            return;
        }
        if (tapsRemaining <= 2) {
            postMessage(tapsRemaining + " more taps to unlock demo mode.");
        }
    }

    public void relockDemoMode() {
        repository.resetDemoMode();
        tapsRemaining = 5;
        publish();
        postMessage("Demo mode locked.");
    }

    public void restartProfileSetup() {
        repository.restartProfileSetup();
        restartProfileEvents.setValue(new Event<>(Boolean.TRUE));
    }

    private void publish() {
        SettingsContent content = repository.getSettingsContent();
        uiState.setValue(new UiState(
                content.isSoundEnabled(),
                content.isHapticsEnabled(),
                content.isMotionEnabled(),
                content.isDemoUnlocked()
        ));
    }

    public static final class UiState {
        private final boolean soundEnabled;
        private final boolean hapticsEnabled;
        private final boolean motionEnabled;
        private final boolean demoUnlocked;

        public UiState(boolean soundEnabled, boolean hapticsEnabled, boolean motionEnabled, boolean demoUnlocked) {
            this.soundEnabled = soundEnabled;
            this.hapticsEnabled = hapticsEnabled;
            this.motionEnabled = motionEnabled;
            this.demoUnlocked = demoUnlocked;
        }

        public boolean isSoundEnabled() {
            return soundEnabled;
        }

        public boolean isHapticsEnabled() {
            return hapticsEnabled;
        }

        public boolean isMotionEnabled() {
            return motionEnabled;
        }

        public boolean isDemoUnlocked() {
            return demoUnlocked;
        }

        public UiState copy(boolean soundEnabled, boolean hapticsEnabled, boolean motionEnabled, boolean demoUnlocked) {
            return new UiState(soundEnabled, hapticsEnabled, motionEnabled, demoUnlocked);
        }
    }
}
