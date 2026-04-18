package com.socialhazard.app.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.IdRes;

import com.socialhazard.app.R;

public final class BackgroundMusicController {

    private static final String TAG = "BackgroundMusic";

    private final Context appContext;

    private MediaPlayer mediaPlayer;
    private Track activeTrack = Track.NONE;
    private Track requestedTrack = Track.LOBBY;
    private boolean musicEnabled = true;
    private boolean started;

    public BackgroundMusicController(Context context) {
        appContext = context.getApplicationContext();
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        syncPlayback();
    }

    public void updateForDestination(@IdRes int destinationId) {
        if (destinationId == R.id.gameFragment
                || destinationId == R.id.judgeFragment
                || destinationId == R.id.roundResultFragment) {
            requestedTrack = Track.MATCH;
        } else {
            requestedTrack = Track.LOBBY;
        }
        syncPlayback();
    }

    public void onStart() {
        started = true;
        syncPlayback();
    }

    public void onStop() {
        started = false;
        pausePlayer();
    }

    public void release() {
        started = false;
        releasePlayer();
    }

    private void syncPlayback() {
        if (!musicEnabled) {
            releasePlayer();
            return;
        }
        if (!started) {
            pausePlayer();
            return;
        }
        if (mediaPlayer == null || activeTrack != requestedTrack) {
            switchTo(requestedTrack);
            return;
        }
        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Unable to resume background music.", exception);
            releasePlayer();
        }
    }

    private void switchTo(Track nextTrack) {
        releasePlayer();
        if (nextTrack == Track.NONE) {
            return;
        }
        try {
            mediaPlayer = MediaPlayer.create(appContext, nextTrack.resourceId);
            if (mediaPlayer == null) {
                Log.w(TAG, "Unable to create player for " + nextTrack.name());
                activeTrack = Track.NONE;
                return;
            }
            mediaPlayer.setLooping(true);
            mediaPlayer.setOnErrorListener((player, what, extra) -> {
                Log.w(TAG, "Background music failed. what=" + what + " extra=" + extra);
                releasePlayer();
                return true;
            });
            activeTrack = nextTrack;
            mediaPlayer.start();
        } catch (RuntimeException exception) {
            Log.w(TAG, "Unable to switch background music.", exception);
            releasePlayer();
        }
    }

    private void pausePlayer() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Unable to pause background music.", exception);
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        activeTrack = Track.NONE;
    }

    private enum Track {
        NONE(0),
        LOBBY(R.raw.june),
        MATCH(R.raw.match_music);

        private final int resourceId;

        Track(int resourceId) {
            this.resourceId = resourceId;
        }
    }
}
