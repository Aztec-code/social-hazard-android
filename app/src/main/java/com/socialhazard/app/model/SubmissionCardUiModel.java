package com.socialhazard.app.model;

public final class SubmissionCardUiModel {

    private final String id;
    private final String label;
    private final String text;
    private final boolean selected;

    public SubmissionCardUiModel(String id, String label, String text, boolean selected) {
        this.id = id;
        this.label = label;
        this.text = text;
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getText() {
        return text;
    }

    public boolean isSelected() {
        return selected;
    }
}
