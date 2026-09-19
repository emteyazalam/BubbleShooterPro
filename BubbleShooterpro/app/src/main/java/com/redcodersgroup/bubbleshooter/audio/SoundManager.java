package com.redcodersgroup.bubbleshooter.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import com.redcodersgroup.bubbleshooter.data.PreferencesManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SoundManager {
    private static SoundManager instance;
    private final Context context;
    private final PreferencesManager prefs;
    private SoundPool soundPool;
    private Vibrator vibrator;

    private int soundShoot = -1;
    private int soundBounce = -1;
    private int soundBomb = -1;
    private int soundWin = -1;
    private int soundClick = -1;
    private final int[] soundPops = new int[6];

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = new PreferencesManager(context);
        this.vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        initSoundPool();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build();

        new Thread(() -> {
            try {
                soundShoot = loadSyntheticSound("snd_shoot.wav", SoundEffectGenerator.generateShoot());
                soundBounce = loadSyntheticSound("snd_bounce.wav", SoundEffectGenerator.generateBounce());
                soundBomb = loadSyntheticSound("snd_bomb.wav", SoundEffectGenerator.generateBomb());
                soundWin = loadSyntheticSound("snd_win.wav", SoundEffectGenerator.generateWin());
                soundClick = loadSyntheticSound("snd_click.wav", SoundEffectGenerator.generateClick());

                // Musical ascending pitch pops for combos (C, D, E, G, A, C)
                float[] pitches = {1.0f, 1.12f, 1.25f, 1.5f, 1.68f, 2.0f};
                for (int i = 0; i < pitches.length; i++) {
                    soundPops[i] = loadSyntheticSound("snd_pop_" + i + ".wav", SoundEffectGenerator.generatePop(pitches[i]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int loadSyntheticSound(String fileName, byte[] pcm) throws IOException {
        File cacheFile = new File(context.getCacheDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
            byte[] header = SoundEffectGenerator.createWavHeader(pcm.length);
            fos.write(header);
            fos.write(pcm);
            fos.flush();
        }
        return soundPool.load(cacheFile.getAbsolutePath(), 1);
    }

    public void playShoot() {
        if (!prefs.isSoundEnabled()) return;
        playSound(soundShoot, 0.85f);
        vibrate(12);
    }

    public void playBounce() {
        if (!prefs.isSoundEnabled()) return;
        playSound(soundBounce, 0.6f);
        vibrate(15);
    }

    public void playPop(int comboIndex) {
        if (!prefs.isSoundEnabled()) return;
        int idx = Math.min(soundPops.length - 1, Math.max(0, comboIndex));
        playSound(soundPops[idx], 1.0f);
        vibrate(20);
    }

    public void playBomb() {
        if (!prefs.isSoundEnabled()) return;
        playSound(soundBomb, 1.0f);
        vibrate(50);
    }

    public void playWin() {
        if (!prefs.isSoundEnabled()) return;
        playSound(soundWin, 1.0f);
        vibrate(70);
    }

    public void playClick() {
        if (!prefs.isSoundEnabled()) return;
        playSound(soundClick, 0.7f);
        vibrate(10);
    }

    private void playSound(int soundId, float volume) {
        if (soundPool != null && soundId > 0) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
        }
    }

    private void vibrate(long durationMs) {
        if (!prefs.isHapticEnabled() || vibrator == null || !vibrator.hasVibrator()) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(durationMs);
            }
        } catch (Exception ignored) {
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
