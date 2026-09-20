package com.redcodersgroup.bubbleshooter.scoring;

public class ScoreManager {
    public static final int BASE_POP_SCORE = 60;
    public static final int BASE_DROP_SCORE = 120;
    public static final int VICTORY_CLEAR_BONUS = 500;
    public static final int REMAINING_SHOT_BONUS = 100;

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

    public int addVictoryBonus(int remainingShots) {
        int bonus = VICTORY_CLEAR_BONUS + Math.max(0, remainingShots) * REMAINING_SHOT_BONUS;
        score += bonus;
        return bonus;
    }

    public int addScore(int points) {
        score += points;
        return points;
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

    /**
     * Calculates star rating upon level completion based on both final score and ball/shot efficiency.
     * Finishing a level with more balls preserved awards higher star ratings.
     */
    public int calculateStars(int shotsRemaining, int initialShots) {
        if (initialShots <= 0) {
            return Math.max(1, getStarsEarned());
        }

        float remainingRatio = (float) shotsRemaining / (float) initialShots;

        // 3 Stars requires high efficiency (at least 35% shots left) and good score, or 28% shots left and top score threshold
        if (remainingRatio >= 0.35f && score >= starThresholds[1]) {
            return 3;
        } else if (remainingRatio >= 0.28f && score >= starThresholds[2]) {
            return 3;
        }

        // 2 Stars requires decent efficiency (at least 15% shots left) and basic score, or 10% shots left and 2-star score threshold
        if (remainingRatio >= 0.15f && score >= starThresholds[0]) {
            return 2;
        } else if (remainingRatio >= 0.10f && score >= starThresholds[1]) {
            return 2;
        }

        // 1 Star: completed level with low remaining shots (< 15%)
        return 1;
    }

    /**
     * Calculates live preview stars during active gameplay.
     */
    public int calculateLiveStars(int shotsRemaining, int initialShots) {
        if (initialShots <= 0) {
            return getStarsEarned();
        }

        float remainingRatio = (float) shotsRemaining / (float) initialShots;
        if (score >= starThresholds[2] && remainingRatio >= 0.28f) {
            return 3;
        } else if (score >= starThresholds[1] && remainingRatio >= 0.15f) {
            return 2;
        } else if (score >= starThresholds[0]) {
            return 1;
        }
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
