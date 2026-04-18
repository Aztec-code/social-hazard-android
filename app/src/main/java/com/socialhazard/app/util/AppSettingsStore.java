package com.socialhazard.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppSettingsStore {

    private static final String PREFS_NAME = "social_hazard_settings";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_HAPTICS_ENABLED = "haptics_enabled";
    private static final String KEY_MOTION_ENABLED = "motion_enabled";

    private final SharedPreferences preferences;

    public AppSettingsStore(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean readSoundEnabled() {
        return preferences.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public boolean readHapticsEnabled() {
        return preferences.getBoolean(KEY_HAPTICS_ENABLED, true);
    }

    public boolean readMotionEnabled() {
        return preferences.getBoolean(KEY_MOTION_ENABLED, true);
    }

    public void saveSoundEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
    }

    public void saveHapticsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply();
    }

    public void saveMotionEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_MOTION_ENABLED, enabled).apply();
    }
}
