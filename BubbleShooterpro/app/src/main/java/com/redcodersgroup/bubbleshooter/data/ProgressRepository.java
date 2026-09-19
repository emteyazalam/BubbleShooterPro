package com.redcodersgroup.bubbleshooter.data;

import android.content.Context;

public class ProgressRepository {
    private static ProgressRepository instance;
    private final PreferencesManager preferencesManager;

    private ProgressRepository(Context context) {
        this.preferencesManager = new PreferencesManager(context);
    }

    public static synchronized ProgressRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ProgressRepository(context.getApplicationContext());
        }
        return instance;
    }

    public PreferencesManager getPreferences() {
        return preferencesManager;
    }

    public boolean isLevelUnlocked(int level) {
        return level <= preferencesManager.getHighestUnlockedLevel();
    }

    public void completeLevel(int level, int stars, int score) {
        int oldStars = preferencesManager.getStarsForLevel(level);
        if (stars > oldStars) {
            preferencesManager.setStarsForLevel(level, stars);
        }
        int oldScore = preferencesManager.getHighScoreForLevel(level);
        if (score > oldScore) {
            preferencesManager.setHighScoreForLevel(level, score);
        }
        preferencesManager.unlockLevel(level + 1);
    }

    public int getTotalStarsEarned(int maxLevels) {
        int total = 0;
        for (int i = 1; i <= maxLevels; i++) {
            total += preferencesManager.getStarsForLevel(i);
        }
        return total;
    }
}
