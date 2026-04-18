package com.socialhazard.app.model;

public final class PlayerSlotUiModel {

    private final String displayName;
    private final String avatarId;
    private final String subtitle;
    private final String seatLabel;
    private final boolean occupied;
    private final boolean host;
    private final int accentColor;

    public PlayerSlotUiModel(
            String displayName,
            String avatarId,
            String subtitle,
            String seatLabel,
            boolean occupied,
            boolean host,
            int accentColor
    ) {
        this.displayName = displayName;
        this.avatarId = avatarId;
        this.subtitle = subtitle;
        this.seatLabel = seatLabel;
        this.occupied = occupied;
        this.host = host;
        this.accentColor = accentColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public boolean isHost() {
        return host;
    }

    public int getAccentColor() {
        return accentColor;
    }
}
