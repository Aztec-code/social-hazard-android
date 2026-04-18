package com.socialhazard.app.model;

public final class SettingsContent {

    private final boolean soundEnabled;
    private final boolean hapticsEnabled;
    private final boolean motionEnabled;
    private final boolean demoUnlocked;

    public SettingsContent(boolean soundEnabled, boolean hapticsEnabled, boolean motionEnabled, boolean demoUnlocked) {
        this.soundEnabled = soundEnabled;
        this.hapticsEnabled = hapticsEnabled;
        this.motionEnabled = motionEnabled;
        this.demoUnlocked = demoUnlocked;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public boolean isHapticsEnabled() {
        return hapticsEnabled;
    }

    public boolean isMotionEnabled() {
        return motionEnabled;
    }

    public boolean isDemoUnlocked() {
        return demoUnlocked;
    }
}
