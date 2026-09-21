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
    private static final String KEY_ENDLESS_HIGH_SCORE = "key_endless_high_score";
    private static final String KEY_PLAYER_NAME = "key_player_name";
    private static final String KEY_PLAYER_AVATAR = "key_player_avatar";
    private static final String KEY_DIAMONDS = "key_player_diamonds";
    private static final String KEY_LIVES = "key_player_lives";
    private static final String KEY_FREE_DIAMONDS_CLAIM_DATE = "key_free_diamonds_claim_date";

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

    public int getEndlessHighScore() {
        return prefs.getInt(KEY_ENDLESS_HIGH_SCORE, 0);
    }

    public void setEndlessHighScore(int score) {
        int current = getEndlessHighScore();
        if (score > current) {
            prefs.edit().putInt(KEY_ENDLESS_HIGH_SCORE, score).apply();
        }
    }

    public boolean hasClaimedWorldGift(int world, int giftIndex) {
        return prefs.getBoolean("world_gift_" + world + "_" + giftIndex, false);
    }

    public void setClaimedWorldGift(int world, int giftIndex, boolean claimed) {
        prefs.edit().putBoolean("world_gift_" + world + "_" + giftIndex, claimed).apply();
    }

    public String getPlayerName() {
        return prefs.getString(KEY_PLAYER_NAME, "Player");
    }

    public void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            prefs.edit().putString(KEY_PLAYER_NAME, name.trim()).apply();
        }
    }

    public String getPlayerAvatar() {
        return prefs.getString(KEY_PLAYER_AVATAR, "avatar_hero");
    }

    public void setPlayerAvatar(String avatarId) {
        if (avatarId != null && !avatarId.trim().isEmpty()) {
            prefs.edit().putString(KEY_PLAYER_AVATAR, avatarId.trim()).apply();
        }
    }

    public int getDiamonds() {
        if (!prefs.contains(KEY_DIAMONDS)) {
            int initial = Math.max(100, getHighestUnlockedLevel() * 25);
            prefs.edit().putInt(KEY_DIAMONDS, initial).apply();
            return initial;
        }
        return prefs.getInt(KEY_DIAMONDS, 100);
    }

    public void setDiamonds(int count) {
        prefs.edit().putInt(KEY_DIAMONDS, Math.max(0, count)).apply();
    }

    public void addDiamonds(int count) {
        if (count > 0) {
            setDiamonds(getDiamonds() + count);
        }
    }

    public boolean spendDiamonds(int cost) {
        if (cost <= 0) return true;
        int current = getDiamonds();
        if (current >= cost) {
            setDiamonds(current - cost);
            return true;
        }
        return false;
    }

    public int getLives() {
        return prefs.getInt(KEY_LIVES, 5);
    }

    public void setLives(int lives) {
        prefs.edit().putInt(KEY_LIVES, Math.max(0, Math.min(5, lives))).apply();
    }

    public void refillLives() {
        setLives(5);
    }

    public void addBombBoosters(int count) {
        setBombBoosters(getBombBoosters() + count);
    }

    public void addRainbowBoosters(int count) {
        setRainbowBoosters(getRainbowBoosters() + count);
    }

    public void addLightningBoosters(int count) {
        setLightningBoosters(getLightningBoosters() + count);
    }

    public void addFireballBoosters(int count) {
        setFireballBoosters(getFireballBoosters() + count);
    }

    public boolean canClaimDailyFreeDiamonds() {
        long lastClaim = prefs.getLong(KEY_FREE_DIAMONDS_CLAIM_DATE, 0);
        long now = System.currentTimeMillis();
        // Allow once every 24 hours (or if never claimed)
        return (now - lastClaim) >= (24L * 60 * 60 * 1000);
    }

    public void markDailyFreeDiamondsClaimed() {
        prefs.edit().putLong(KEY_FREE_DIAMONDS_CLAIM_DATE, System.currentTimeMillis()).apply();
    }

    public void resetProgress() {
        prefs.edit().clear().apply();
    }
}
