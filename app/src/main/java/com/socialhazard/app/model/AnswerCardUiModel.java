package com.socialhazard.app.model;

public final class AnswerCardUiModel {

    private final String id;
    private final String text;
    private final String footer;
    private final boolean selected;

    public AnswerCardUiModel(String id, String text, String footer, boolean selected) {
        this.id = id;
        this.text = text;
        this.footer = footer;
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getFooter() {
        return footer;
    }

    public boolean isSelected() {
        return selected;
    }
}
