package com.redcodersgroup.bubbleshooter.scoring;

public class ScoreManager {
    public static final int BASE_POP_SCORE = 10;
    public static final int BASE_DROP_SCORE = 25;

    private int score = 0;
    private int[] starThresholds = new int[]{1000, 2500, 4500};

    public void setStarThresholds(int[] thresholds) {
        if (thresholds != null && thresholds.length >= 3) {
            this.starThresholds = thresholds;
        }
    }

    public int addPoppedBubbles(int count, int multiplier) {
        int earned = count * BASE_POP_SCORE * Math.max(1, multiplier);
        score += earned;
        return earned;
    }

    public int addDroppedBubbles(int count, int multiplier) {
        int earned = count * BASE_DROP_SCORE * Math.max(1, multiplier);
        score += earned;
        return earned;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getStarsEarned() {
        if (score >= starThresholds[2]) return 3;
        if (score >= starThresholds[1]) return 2;
        if (score >= starThresholds[0]) return 1;
        return 0;
    }

    public float getStarProgress() {
        int max = starThresholds[2];
        if (max <= 0) return 0f;
        return Math.min(1.0f, (float) score / max);
    }

    public int[] getStarThresholds() {
        return starThresholds;
    }

    public void reset() {
        score = 0;
    }
}
