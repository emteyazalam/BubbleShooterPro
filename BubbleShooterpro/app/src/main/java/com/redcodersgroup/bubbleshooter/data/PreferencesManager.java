package com.redcodersgroup.bubbleshooter.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "bubble_shooter_prefs";
    private static final String KEY_SOUND = "key_sound_enabled";
    private static final String KEY_MUSIC = "key_music_enabled";
    private static final String KEY_HAPTIC = "key_haptic_enabled";
    private static final String KEY_HIGHEST_LEVEL = "key_highest_unlocked_level";
    private static final String KEY_STARS_PREFIX = "key_stars_lvl_";
    private static final String KEY_SCORE_PREFIX = "key_score_lvl_";
    private static final String KEY_BOOSTER_BOMB = "key_booster_bomb";
    private static final String KEY_BOOSTER_RAINBOW = "key_booster_rainbow";
    private static final String KEY_BOOSTER_LIGHTNING = "key_booster_lightning";
    private static final String KEY_BOOSTER_FIREBALL = "key_booster_fireball";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean(KEY_SOUND, true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public boolean isMusicEnabled() {
        return prefs.getBoolean(KEY_MUSIC, true);
    }

    public void setMusicEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MUSIC, enabled).apply();
    }

    public boolean isHapticEnabled() {
        return prefs.getBoolean(KEY_HAPTIC, true);
    }

    public void setHapticEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply();
    }

    public int getHighestUnlockedLevel() {
        return prefs.getInt(KEY_HIGHEST_LEVEL, 1);
    }

    public void unlockLevel(int level) {
        if (level > getHighestUnlockedLevel()) {
            prefs.edit().putInt(KEY_HIGHEST_LEVEL, level).apply();
        }
    }

    public int getStarsForLevel(int level) {
        return prefs.getInt(KEY_STARS_PREFIX + level, 0);
    }

    public void setStarsForLevel(int level, int stars) {
        int current = getStarsForLevel(level);
        if (stars > current) {
            prefs.edit().putInt(KEY_STARS_PREFIX + level, stars).apply();
        }
    }

    public int getHighScoreForLevel(int level) {
        return prefs.getInt(KEY_SCORE_PREFIX + level, 0);
    }

    public void setHighScoreForLevel(int level, int score) {
        int current = getHighScoreForLevel(level);
        if (score > current) {
            prefs.edit().putInt(KEY_SCORE_PREFIX + level, score).apply();
        }
    }

    public int getBombBoosters() {
        return prefs.getInt(KEY_BOOSTER_BOMB, 99);
    }

    public void setBombBoosters(int count) {
        prefs.edit().putInt(KEY_BOOSTER_BOMB, count).apply();
    }

    public int getRainbowBoosters() {
        return prefs.getInt(KEY_BOOSTER_RAINBOW, 99);
    }

    public void setRainbowBoosters(int count) {
        prefs.edit().putInt(KEY_BOOSTER_RAINBOW, count).apply();
    }

    public int getLightningBoosters() {
        return prefs.getInt(KEY_BOOSTER_LIGHTNING, 99);
    }

    public void setLightningBoosters(int count) {
        prefs.edit().putInt(KEY_BOOSTER_LIGHTNING, count).apply();
    }

    public int getFireballBoosters() {
        return prefs.getInt(KEY_BOOSTER_FIREBALL, 99);
    }

    public void setFireballBoosters(int count) {
        prefs.edit().putInt(KEY_BOOSTER_FIREBALL, count).apply();
    }

    public void resetProgress() {
        prefs.edit().clear().apply();
    }
}
