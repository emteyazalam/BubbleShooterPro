package com.redcodersgroup.bubbleshooter.scoring;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScoreManagerTest {

    private ScoreManager scoreManager;
    private ComboManager comboManager;

    @Before
    public void setUp() {
        scoreManager = new ScoreManager();
        comboManager = new ComboManager();
    }

    @Test
    public void testBasePoppedScore() {
        // 3 bubbles popped with multiplier 1 (3 * 60 = 180)
        int earned = scoreManager.addPoppedBubbles(3, 1);
        assertEquals(180, earned);
        assertEquals(180, scoreManager.getScore());
    }

    @Test
    public void testComboMultiplier() {
        comboManager.registerSuccess(); // streak 1 -> 1x
        assertEquals(1, comboManager.getMultiplier());

        comboManager.registerSuccess(); // streak 2 -> 2x
        assertEquals(2, comboManager.getMultiplier());

        int earned = scoreManager.addPoppedBubbles(3, comboManager.getMultiplier());
        assertEquals(360, earned); // 3 * 60 * 2 = 360
    }

    @Test
    public void testVictoryBonus() {
        // 10 shots remaining -> 500 clear bonus + 10 * 100 = 1500
        int bonus = scoreManager.addVictoryBonus(10);
        assertEquals(1500, bonus);
        assertEquals(1500, scoreManager.getScore());
    }

    @Test
    public void testStarThresholds() {
        scoreManager.setStarThresholds(new int[]{1000, 2000, 3000});

        scoreManager.setScore(500);
        assertEquals(0, scoreManager.getStarsEarned());

        scoreManager.setScore(1000);
        assertEquals(1, scoreManager.getStarsEarned());

        scoreManager.setScore(2500);
        assertEquals(2, scoreManager.getStarsEarned());

        scoreManager.setScore(3500);
        assertEquals(3, scoreManager.getStarsEarned());
    }
}
