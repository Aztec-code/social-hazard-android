package com.socialhazard.app.network;

import androidx.annotation.Nullable;

public interface GameSocketClient {

    interface Listener {
        void onConnected();

        void onMessage(String text);

        void onDisconnected(@Nullable String reason);

        void onFailure(String message);
    }

    void setListener(Listener listener);

    void connect();

    void disconnect();

    boolean isConnected();

    boolean isConnecting();

    boolean send(String text);
}
