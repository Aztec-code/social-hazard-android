package com.socialhazard.app.model;

public final class PlayerProfile {

    private final String nickname;
    private final String avatarId;

    public PlayerProfile(String nickname, String avatarId) {
        this.nickname = nickname == null ? "" : nickname;
        this.avatarId = avatarId == null ? "" : avatarId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public boolean isComplete() {
        return nickname.trim().length() >= 2 && !avatarId.isBlank();
    }
}
