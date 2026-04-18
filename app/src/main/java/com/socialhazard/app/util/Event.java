package com.socialhazard.app.util;

import androidx.annotation.Nullable;

public final class Event<T> {

    private final T value;
    private boolean handled;

    public Event(T value) {
        this.value = value;
    }

    @Nullable
    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return value;
    }

    public T peek() {
        return value;
    }
}
