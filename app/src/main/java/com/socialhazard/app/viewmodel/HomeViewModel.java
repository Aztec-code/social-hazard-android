package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.socialhazard.app.model.HomeContent;
import com.socialhazard.app.model.PlayerProfile;
import com.socialhazard.app.repository.GameRepository;

public final class HomeViewModel extends BaseViewModel {

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();

    public HomeViewModel(GameRepository repository) {
        HomeContent content = repository.getHomeContent();
        PlayerProfile profile = repository.getPlayerProfile();
        uiState.setValue(new UiState(
                content.getEyebrow(),
                content.getTitle(),
                content.getSubtitle(),
                content.getRoomHint(),
                profile.getAvatarId()
        ));
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public static final class UiState {
        private final String eyebrow;
        private final String title;
        private final String subtitle;
        private final String roomHint;
        private final String avatarId;

        public UiState(String eyebrow, String title, String subtitle, String roomHint, String avatarId) {
            this.eyebrow = eyebrow;
            this.title = title;
            this.subtitle = subtitle;
            this.roomHint = roomHint;
            this.avatarId = avatarId;
        }

        public String getEyebrow() {
            return eyebrow;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getRoomHint() {
            return roomHint;
        }

        public String getAvatarId() {
            return avatarId;
        }
    }
}
