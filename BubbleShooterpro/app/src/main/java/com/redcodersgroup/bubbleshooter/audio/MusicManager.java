package com.redcodersgroup.bubbleshooter.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;
import com.redcodersgroup.bubbleshooter.R;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;

public class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager instance;
    private final Context context;
    private final PreferencesManager prefs;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private int activeActivities = 0;

    private MusicManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = new PreferencesManager(context);
    }

    public static synchronized MusicManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicManager(context);
        }
        return instance;
    }

    public synchronized void startMusic() {
        if (!prefs.isMusicEnabled()) {
            return;
        }

        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.bgm);
                if (mediaPlayer == null) {
                    Log.e(TAG, "Failed to create MediaPlayer for bgm");
                    return;
                }
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0.60f, 0.60f);
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                    releaseMediaPlayer();
                    return true;
                });
            }

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                isPlaying = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting background music", e);
            releaseMediaPlayer();
        }
    }

    public synchronized void pauseMusic() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } catch (Exception ignored) {}
        }
        isPlaying = false;
    }

    public synchronized void resumeMusic() {
        if (prefs.isMusicEnabled()) {
            startMusic();
        }
    }

    public synchronized void stopMusic() {
        releaseMediaPlayer();
        isPlaying = false;
    }

    public synchronized void setMusicEnabled(boolean enabled) {
        prefs.setMusicEnabled(enabled);
        if (enabled) {
            startMusic();
        } else {
            pauseMusic();
        }
    }

    public boolean isMusicEnabled() {
        return prefs.isMusicEnabled();
    }

    public synchronized void onActivityStarted() {
        activeActivities++;
        if (activeActivities == 1 && prefs.isMusicEnabled()) {
            startMusic();
        }
    }

    public synchronized void onActivityStopped() {
        activeActivities = Math.max(0, activeActivities - 1);
        if (activeActivities == 0) {
            pauseMusic();
        }
    }

    private synchronized void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {}
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
        isPlaying = false;
    }
}
