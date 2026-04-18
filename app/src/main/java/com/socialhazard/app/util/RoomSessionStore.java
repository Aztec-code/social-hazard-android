package com.socialhazard.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.socialhazard.app.model.RoomSession;

public final class RoomSessionStore {

    private static final String PREFS_NAME = "social_hazard_room_session";
    private static final String KEY_ROOM_CODE = "room_code";
    private static final String KEY_PLAYER_ID = "player_id";
    private static final String KEY_PLAYER_TOKEN = "player_token";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_AVATAR_ID = "avatar_id";

    private final SharedPreferences preferences;

    public RoomSessionStore(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    public RoomSession read() {
        String roomCode = preferences.getString(KEY_ROOM_CODE, null);
        String playerId = preferences.getString(KEY_PLAYER_ID, null);
        String playerToken = preferences.getString(KEY_PLAYER_TOKEN, null);
        String displayName = preferences.getString(KEY_DISPLAY_NAME, null);
        String avatarId = preferences.getString(KEY_AVATAR_ID, null);
        if (roomCode == null || playerId == null || playerToken == null) {
            return null;
        }
        return new RoomSession(
                roomCode,
                playerId,
                playerToken,
                displayName == null ? "" : displayName,
                avatarId == null ? "" : avatarId
        );
    }

    public void save(RoomSession session) {
        preferences.edit()
                .putString(KEY_ROOM_CODE, session.getRoomCode())
                .putString(KEY_PLAYER_ID, session.getPlayerId())
                .putString(KEY_PLAYER_TOKEN, session.getPlayerToken())
                .putString(KEY_DISPLAY_NAME, session.getDisplayName())
                .putString(KEY_AVATAR_ID, session.getAvatarId())
                .apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
