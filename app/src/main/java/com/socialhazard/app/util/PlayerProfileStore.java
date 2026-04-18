package com.socialhazard.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.socialhazard.app.model.PlayerProfile;

public final class PlayerProfileStore {

    private static final String PREFS_NAME = "social_hazard_profile";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_AVATAR_ID = "avatar_id";

    private final SharedPreferences preferences;

    public PlayerProfileStore(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    public PlayerProfile read() {
        String nickname = preferences.getString(KEY_NICKNAME, null);
        String avatarId = preferences.getString(KEY_AVATAR_ID, null);
        if (nickname == null || avatarId == null) {
            return null;
        }
        PlayerProfile profile = new PlayerProfile(nickname, avatarId);
        return profile.isComplete() ? profile : null;
    }

    public void save(PlayerProfile profile) {
        preferences.edit()
                .putString(KEY_NICKNAME, profile.getNickname())
                .putString(KEY_AVATAR_ID, profile.getAvatarId())
                .apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
