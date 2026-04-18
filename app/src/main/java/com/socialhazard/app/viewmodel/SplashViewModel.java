package com.socialhazard.app.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.socialhazard.app.repository.GameRepository;
import com.socialhazard.app.util.Event;

public final class SplashViewModel extends BaseViewModel {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final GameRepository repository;
    private final MutableLiveData<Event<Destination>> navigationEvents = new MutableLiveData<>();
    private boolean started;

    public SplashViewModel(GameRepository repository) {
        this.repository = repository;
    }

    public LiveData<Event<Destination>> getNavigationEvents() {
        return navigationEvents;
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        handler.postDelayed(() -> navigationEvents.setValue(new Event<>(
                repository.hasPlayerProfile() ? Destination.HOME : Destination.PROFILE
        )), 1200L);
    }

    public enum Destination {
        HOME,
        PROFILE
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacksAndMessages(null);
        super.onCleared();
    }
}
