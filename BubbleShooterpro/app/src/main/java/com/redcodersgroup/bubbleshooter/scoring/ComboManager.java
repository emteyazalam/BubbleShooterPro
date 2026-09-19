package com.redcodersgroup.bubbleshooter.scoring;

public class ComboManager {
    private int currentStreak = 0;

    public void registerSuccess() {
        currentStreak++;
    }

    public void registerMiss() {
        currentStreak = 0;
    }

    public int getMultiplier() {
        if (currentStreak <= 1) return 1;
        if (currentStreak == 2) return 2;
        if (currentStreak == 3) return 3;
        if (currentStreak == 4) return 4;
        return 5;
    }

    public int getStreak() {
        return currentStreak;
    }

    public String getPraiseText() {
        switch (currentStreak) {
            case 2: return "GREAT! x2";
            case 3: return "AWESOME! x3";
            case 4: return "SUPER! x4";
            case 5: return "FANTASTIC! x5";
            default:
                if (currentStreak > 5) return "UNSTOPPABLE! x" + getMultiplier();
                return "";
        }
    }

    public void reset() {
        currentStreak = 0;
    }
}
