package com.socialhazard.app.config;

import com.socialhazard.app.BuildConfig;

public final class AppEnvironment {

    public static final String PRODUCTION_BASE_URL = "https://api.fluxcloud.dev";
    public static final String PRODUCTION_WS_URL = "wss://api.fluxcloud.dev/ws";
    public static final String BASE_URL = BuildConfig.BASE_URL;
    public static final String WS_URL = BuildConfig.WS_URL;
    public static final boolean USE_LOCAL_BACKEND = BuildConfig.USE_LOCAL_BACKEND;
    public static final String BACKEND_MODE = BuildConfig.BACKEND_MODE;

    private AppEnvironment() {
    }

    public static String baseUrl() {
        return BASE_URL;
    }

    public static String websocketUrl() {
        return WS_URL;
    }

    public static String healthUrl() {
        return baseUrl() + "/health";
    }

    public static boolean isProductionBackend() {
        return !USE_LOCAL_BACKEND;
    }
}
