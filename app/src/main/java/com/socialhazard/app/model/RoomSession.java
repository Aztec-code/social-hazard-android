package com.socialhazard.app.model;

public final class RoomSession {

    private final String roomCode;
    private final String playerId;
    private final String playerToken;
    private final String displayName;
    private final String avatarId;

    public RoomSession(String roomCode, String playerId, String playerToken, String displayName, String avatarId) {
        this.roomCode = roomCode;
        this.playerId = playerId;
        this.playerToken = playerToken;
        this.displayName = displayName;
        this.avatarId = avatarId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarId() {
        return avatarId;
    }
}
