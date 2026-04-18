package com.socialhazard.app.network;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public final class OkHttpGameSocketClient implements GameSocketClient {

    private static final String TAG = "GameSocketClient";

    private final OkHttpClient okHttpClient;
    private final String websocketUrl;
    private final Request request;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Listener listener;
    private WebSocket webSocket;
    private boolean connected;
    private boolean connecting;
    private boolean manualClose;

    public OkHttpGameSocketClient(OkHttpClient okHttpClient, String websocketUrl) {
        this.okHttpClient = okHttpClient;
        this.websocketUrl = websocketUrl;
        this.request = new Request.Builder().url(websocketUrl).build();
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public synchronized void connect() {
        if (connected || connecting) {
            return;
        }
        manualClose = false;
        connecting = true;
        Log.i(TAG, "Opening WebSocket connection to " + websocketUrl);
        webSocket = okHttpClient.newWebSocket(request, new SocketListener());
    }

    @Override
    public synchronized void disconnect() {
        manualClose = true;
        connected = false;
        connecting = false;
        Log.i(TAG, "Closing WebSocket connection.");
        if (webSocket != null) {
            webSocket.close(1000, "client_closed");
            webSocket = null;
        }
    }

    @Override
    public synchronized boolean isConnected() {
        return connected;
    }

    @Override
    public synchronized boolean isConnecting() {
        return connecting;
    }

    @Override
    public synchronized boolean send(String text) {
        return webSocket != null && connected && webSocket.send(text);
    }

    private final class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            synchronized (OkHttpGameSocketClient.this) {
                connected = true;
                connecting = false;
            }
            Log.i(TAG, "WebSocket connected. HTTP " + response.code());
            dispatchConnected();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            dispatchMessage(text);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(code, reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            synchronized (OkHttpGameSocketClient.this) {
                connected = false;
                connecting = false;
                OkHttpGameSocketClient.this.webSocket = null;
            }
            Log.i(TAG, "WebSocket closed. code=" + code + " reason=" + reason);
            if (!manualClose) {
                dispatchDisconnected(reason);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
            synchronized (OkHttpGameSocketClient.this) {
                connected = false;
                connecting = false;
                OkHttpGameSocketClient.this.webSocket = null;
            }
            String responseCode = response == null ? "n/a" : String.valueOf(response.code());
            String failureMessage = t.getMessage() == null ? "WebSocket connection failed." : t.getMessage();
            Log.e(TAG, "WebSocket failure. responseCode=" + responseCode + " message=" + failureMessage, t);
            if (!manualClose) {
                dispatchFailure(failureMessage);
            }
        }
    }

    private void dispatchConnected() {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onConnected();
            }
        });
    }

    private void dispatchMessage(String text) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onMessage(text);
            }
        });
    }

    private void dispatchDisconnected(@Nullable String reason) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onDisconnected(reason);
            }
        });
    }

    private void dispatchFailure(String message) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onFailure(message);
            }
        });
    }
}
