package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.socialhazard.app.model.DemoScene;
import com.socialhazard.app.repository.GameRepository;

public final class DemoModeViewModel extends BaseViewModel {

    private final GameRepository repository;
    private final MediatorLiveData<UiState> uiState = new MediatorLiveData<>();

    public DemoModeViewModel(GameRepository repository) {
        this.repository = repository;
        uiState.addSource(repository.observeDemoUnlocked(), value -> publish());
        uiState.addSource(repository.observeDemoActive(), value -> publish());
        publish();
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void startSoloWalkthrough() {
        repository.startSoloDemo();
    }

    public void openScene(DemoScene scene) {
        repository.showDemoScene(scene);
    }

    private void publish() {
        Boolean unlocked = repository.observeDemoUnlocked().getValue();
        Boolean active = repository.observeDemoActive().getValue();
        uiState.setValue(new UiState(
                unlocked != null && unlocked,
                active != null && active
        ));
    }

    public static final class UiState {
        private final boolean unlocked;
        private final boolean active;

        public UiState(boolean unlocked, boolean active) {
            this.unlocked = unlocked;
            this.active = active;
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        public boolean isActive() {
            return active;
        }
    }
}
