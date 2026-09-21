package com.redcodersgroup.bubbleshooter.level;

import com.redcodersgroup.bubbleshooter.board.BubbleBoard;
import com.redcodersgroup.bubbleshooter.bubble.BubbleColor;
import com.redcodersgroup.bubbleshooter.scoring.ScoreManager;

public class LevelObjective {
    public enum Type {
        CLEAR_ALL,
        DROP_COUNT,
        SCORE_TARGET,
        POP_COLOR
    }

    private final Type type;
    private final int targetValue;
    private final BubbleColor targetColor;
    private int currentProgress = 0;

    public LevelObjective(Type type, int targetValue, BubbleColor targetColor) {
        this.type = type;
        this.targetValue = targetValue;
        this.targetColor = targetColor;
    }

    public static LevelObjective clearAll() {
        return new LevelObjective(Type.CLEAR_ALL, 0, BubbleColor.NONE);
    }

    public static LevelObjective dropCount(int count) {
        return new LevelObjective(Type.DROP_COUNT, count, BubbleColor.NONE);
    }

    public static LevelObjective scoreTarget(int target) {
        return new LevelObjective(Type.SCORE_TARGET, target, BubbleColor.NONE);
    }

    public static LevelObjective popColor(BubbleColor color, int count) {
        return new LevelObjective(Type.POP_COLOR, count, color);
    }

    public Type getType() {
        return type;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public BubbleColor getTargetColor() {
        return targetColor;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void addProgress(int amount) {
        this.currentProgress += amount;
    }

    public void resetProgress() {
        this.currentProgress = 0;
    }

    public boolean isMet(BubbleBoard board, ScoreManager scoreManager) {
        switch (type) {
            case CLEAR_ALL:
                return board.getGrid().getBubbleCount() == 0;
            case DROP_COUNT:
            case POP_COLOR:
                return currentProgress >= targetValue;
            case SCORE_TARGET:
                return scoreManager.getScore() >= targetValue;
            default:
                return board.getGrid().getBubbleCount() == 0;
        }
    }


    public String getBadgeText(int currentScore, int remainingBubbles) {
        switch (type) {
            case CLEAR_ALL:
                return "🎯 CLEAR ALL: " + remainingBubbles + " LEFT";
            case DROP_COUNT:
                return "🎯 DROP: " + currentProgress + "/" + targetValue;
            case POP_COLOR:
                return "🎯 POP " + targetColor.name() + ": " + currentProgress + "/" + targetValue;
            case SCORE_TARGET:
                return "🎯 GOAL: " + String.format("%,d", currentScore) + "/" + String.format("%,d", targetValue);
            default:
                return "🎯 PUZZLE: " + remainingBubbles + " LEFT";
        }
    }

    public String getCompletedSummaryText() {
        switch (type) {
            case CLEAR_ALL:
                return "All bubbles cleared!";
            case DROP_COUNT:
                return "Dropped " + targetValue + " bubbles!";
            case SCORE_TARGET:
                return "Target score of " + String.format("%,d", targetValue) + " reached!";
            case POP_COLOR:
                return "Popped " + targetValue + " " + targetColor.name() + " bubbles!";
            default:
                return "Level objective complete!";
        }
    }
}
