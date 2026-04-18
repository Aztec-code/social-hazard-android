package com.socialhazard.app.model;

public final class RoomEntryContent {

    private final boolean createMode;
    private final String headline;
    private final String body;
    private final String displayName;
    private final String roomCode;
    private final int targetScore;
    private final boolean duelMode;

    public RoomEntryContent(
            boolean createMode,
            String headline,
            String body,
            String displayName,
            String roomCode,
            int targetScore,
            boolean duelMode
    ) {
        this.createMode = createMode;
        this.headline = headline;
        this.body = body;
        this.displayName = displayName;
        this.roomCode = roomCode;
        this.targetScore = targetScore;
        this.duelMode = duelMode;
    }

    public boolean isCreateMode() {
        return createMode;
    }

    public String getHeadline() {
        return headline;
    }

    public String getBody() {
        return body;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public boolean isDuelMode() {
        return duelMode;
    }
}
