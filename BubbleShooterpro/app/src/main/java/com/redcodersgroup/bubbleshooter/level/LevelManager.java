package com.redcodersgroup.bubbleshooter.level;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;

public class LevelManager {
    public static final int TOTAL_LEVELS = 490;
    private static LevelManager instance;
    private final Context context;
    private final Map<Integer, Level> levelCache = new HashMap<>();

    private LevelManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized LevelManager getInstance(Context context) {
        if (instance == null) {
            instance = new LevelManager(context);
        }
        return instance;
    }

    public Level getLevel(int levelNumber) {
        if (levelNumber < 1) levelNumber = 1;
        if (levelNumber > TOTAL_LEVELS) levelNumber = TOTAL_LEVELS;

        if (levelCache.containsKey(levelNumber)) {
            return levelCache.get(levelNumber);
        }

        Level level = LevelLoader.loadLevelFromAssets(context, levelNumber);
        levelCache.put(levelNumber, level);
        return level;
    }

    public int getTotalLevels() {
        return TOTAL_LEVELS;
    }
}
