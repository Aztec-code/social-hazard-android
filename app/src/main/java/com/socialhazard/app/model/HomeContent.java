package com.socialhazard.app.model;

public final class HomeContent {

    private final String eyebrow;
    private final String title;
    private final String subtitle;
    private final String roomHint;

    public HomeContent(String eyebrow, String title, String subtitle, String roomHint) {
        this.eyebrow = eyebrow;
        this.title = title;
        this.subtitle = subtitle;
        this.roomHint = roomHint;
    }

    public String getEyebrow() {
        return eyebrow;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getRoomHint() {
        return roomHint;
    }
}
