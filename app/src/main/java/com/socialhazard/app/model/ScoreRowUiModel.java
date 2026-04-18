package com.socialhazard.app.model;

public final class ScoreRowUiModel {

    private final String rank;
    private final String displayName;
    private final String avatarId;
    private final String summary;
    private final int score;
    private final boolean highlighted;

    public ScoreRowUiModel(String rank, String displayName, String avatarId, String summary, int score, boolean highlighted) {
        this.rank = rank;
        this.displayName = displayName;
        this.avatarId = avatarId;
        this.summary = summary;
        this.score = score;
        this.highlighted = highlighted;
    }

    public String getRank() {
        return rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public String getSummary() {
        return summary;
    }

    public int getScore() {
        return score;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
}
