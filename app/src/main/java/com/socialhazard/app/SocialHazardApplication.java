package com.socialhazard.app;

import android.app.Application;
import android.util.Log;

import com.socialhazard.app.config.AppEnvironment;
import com.socialhazard.app.network.GameSocketClient;
import com.socialhazard.app.network.OkHttpGameSocketClient;
import com.socialhazard.app.network.SocketMessageAdapter;
import com.socialhazard.app.repository.DefaultGameRepository;
import com.socialhazard.app.repository.GameRepository;
import com.socialhazard.app.util.AppSettingsStore;
import com.socialhazard.app.util.PlayerProfileStore;
import com.socialhazard.app.util.RequestIdGenerator;
import com.socialhazard.app.util.RoomSessionStore;
import com.socialhazard.app.viewmodel.ViewModelFactory;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class SocialHazardApplication extends Application {

    private static final String TAG = "SocialHazardApp";

    private GameRepository gameRepository;
    private ViewModelFactory viewModelFactory;

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .pingInterval(20, TimeUnit.SECONDS)
                .build();
        Log.i(TAG, "Initializing backend mode=" + AppEnvironment.BACKEND_MODE
                + " baseUrl=" + AppEnvironment.baseUrl()
                + " wsUrl=" + AppEnvironment.websocketUrl());
        GameSocketClient socketClient = new OkHttpGameSocketClient(okHttpClient, AppEnvironment.websocketUrl());
        SocketMessageAdapter messageAdapter = new SocketMessageAdapter(new RequestIdGenerator());
        RoomSessionStore roomSessionStore = new RoomSessionStore(this);
        PlayerProfileStore profileStore = new PlayerProfileStore(this);
        AppSettingsStore settingsStore = new AppSettingsStore(this);

        gameRepository = new DefaultGameRepository(socketClient, messageAdapter, roomSessionStore, profileStore, settingsStore);
        viewModelFactory = new ViewModelFactory(gameRepository);
    }

    public GameRepository getGameRepository() {
        return gameRepository;
    }

    public ViewModelFactory getViewModelFactory() {
        return viewModelFactory;
    }
}
