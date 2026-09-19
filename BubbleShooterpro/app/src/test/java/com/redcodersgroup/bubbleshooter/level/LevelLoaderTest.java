package com.redcodersgroup.bubbleshooter.level;

import com.redcodersgroup.bubbleshooter.bubble.BubbleColor;
import org.junit.Test;
import static org.junit.Assert.*;

public class LevelLoaderTest {

    @Test
    public void testFallbackLevelCreation() {
        Level fallback = LevelLoader.createFallbackLevel(1);
        assertNotNull(fallback);
        assertEquals(1, fallback.getLevelNumber());
        assertTrue(fallback.getMaxShots() > 0);
        assertFalse(fallback.getRows().isEmpty());
        assertEquals(LevelObjective.Type.CLEAR_ALL, fallback.getObjective().getType());
        assertTrue(fallback.getAvailableColors().contains(BubbleColor.RED));
    }
}
