package com.redcodersgroup.bubbleshooter.level;

import com.redcodersgroup.bubbleshooter.bubble.BubbleColor;
import java.util.List;

public class Level {
    private final int levelNumber;
    private final int maxShots;
    private final List<BubbleColor> availableColors;
    private final LevelObjective objective;
    private final int[] starThresholds;
    private final List<String> rows;

    public Level(int levelNumber, int maxShots, List<BubbleColor> availableColors,
                 LevelObjective objective, int[] starThresholds, List<String> rows) {
        this.levelNumber = levelNumber;
        this.maxShots = maxShots;
        this.availableColors = availableColors;
        this.objective = objective;
        this.starThresholds = starThresholds;
        this.rows = rows;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getMaxShots() {
        return maxShots;
    }

    public List<BubbleColor> getAvailableColors() {
        return availableColors;
    }

    public LevelObjective getObjective() {
        return objective;
    }

    public int[] getStarThresholds() {
        return starThresholds;
    }

    public List<String> getRows() {
        return rows;
    }
}
