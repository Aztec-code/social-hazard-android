package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.socialhazard.app.model.PlayerProfile;
import com.socialhazard.app.repository.GameRepository;
import com.socialhazard.app.util.AvatarCatalog;
import com.socialhazard.app.util.Event;

public final class ProfileSetupViewModel extends BaseViewModel {

    private final GameRepository repository;
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> continueEvents = new MutableLiveData<>();

    private String nickname;
    private String avatarId;
    private String nicknameError;

    public ProfileSetupViewModel(GameRepository repository) {
        this.repository = repository;
        PlayerProfile existing = repository.getPlayerProfile();
        this.nickname = existing.getNickname();
        this.avatarId = AvatarCatalog.isValid(existing.getAvatarId()) ? existing.getAvatarId() : AvatarCatalog.defaultAvatarId();
        publish();
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public LiveData<Event<Boolean>> getContinueEvents() {
        return continueEvents;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? "" : nickname;
        this.nicknameError = null;
        publish();
    }

    public void showPreviousAvatar() {
        avatarId = AvatarCatalog.previous(avatarId);
        publish();
    }

    public void showNextAvatar() {
        avatarId = AvatarCatalog.next(avatarId);
        publish();
    }

    public void saveProfile() {
        String normalized = nickname == null ? "" : nickname.trim();
        if (normalized.length() < 2) {
            nicknameError = "Use at least 2 characters.";
            publish();
            return;
        }
        repository.savePlayerProfile(new PlayerProfile(normalized, avatarId));
        continueEvents.setValue(new Event<>(Boolean.TRUE));
    }

    private void publish() {
        uiState.setValue(new UiState(nickname, avatarId, nicknameError));
    }

    public static final class UiState {
        private final String nickname;
        private final String avatarId;
        private final String nicknameError;

        public UiState(String nickname, String avatarId, String nicknameError) {
            this.nickname = nickname;
            this.avatarId = avatarId;
            this.nicknameError = nicknameError;
        }

        public String getNickname() {
            return nickname;
        }

        public String getAvatarId() {
            return avatarId;
        }

        public String getNicknameError() {
            return nicknameError;
        }
    }
}
