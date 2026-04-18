package com.socialhazard.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.socialhazard.app.util.Event;

public abstract class BaseViewModel extends ViewModel {

    private final MutableLiveData<Event<String>> messages = new MutableLiveData<>();

    public LiveData<Event<String>> getMessages() {
        return messages;
    }

    protected void postMessage(String message) {
        messages.setValue(new Event<>(message));
    }
}
