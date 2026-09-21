package com.redcodersgroup.bubbleshooter.physics;

import android.graphics.PointF;
import com.redcodersgroup.bubbleshooter.board.BubbleGrid;
import com.redcodersgroup.bubbleshooter.board.GridPosition;
import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import com.redcodersgroup.bubbleshooter.bubble.BubbleColor;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

public class TrajectoryCalculatorTest {

    @Test
    public void testNormalTrajectoryCollidesWithBubble() {
        BubbleGrid grid = new BubbleGrid();
        grid.setDimensions(0f, 100f, 40f);

        // Place a bubble at row 5, col 4 (directly above center)
        Bubble b = new Bubble(BubbleColor.RED, new GridPosition(5, 4));
        grid.setBubble(5, 4, b);

        float launcherX = grid.getCenterX(5, 4);
        float launcherY = 1500f;
        float angleRad = (float) (-Math.PI / 2.0); // Straight up

        List<PointF> normalPoints = TrajectoryCalculator.calculateTrajectory(
                launcherX, launcherY, angleRad,
                0f, 800f, 100f,
                grid, 40f, false
        );

        // Normal trajectory must stop near the bubble
        assertFalse(normalPoints.isEmpty());
        PointF lastNormalPoint = normalPoints.get(normalPoints.size() - 1);
        assertTrue(lastNormalPoint.y > 100f + 40f); // Did not reach ceiling because it hit bubble
    }

    @Test
    public void testFireballPiercingTrajectoryReachesCeiling() {
        BubbleGrid grid = new BubbleGrid();
        grid.setDimensions(0f, 100f, 40f);

        // Fill row 5 with bubbles
        for (int c = 0; c < BubbleGrid.COLS_EVEN; c++) {
            grid.setBubble(5, c, new Bubble(BubbleColor.BLUE, new GridPosition(5, c)));
        }

        float launcherX = grid.getCenterX(5, 4);
        float launcherY = 1500f;
        float angleRad = (float) (-Math.PI / 2.0); // Straight up

        List<PointF> piercingPoints = TrajectoryCalculator.calculateTrajectory(
                launcherX, launcherY, angleRad,
                0f, 800f, 100f,
                grid, 40f, true
        );

        // Piercing trajectory must pierce through row 5 and reach ceiling
        assertFalse(piercingPoints.isEmpty());
        PointF lastPiercingPoint = piercingPoints.get(piercingPoints.size() - 1);
        assertEquals(100f + 40f, lastPiercingPoint.y, 1.0f); // Reached ceiling
    }

    @Test
    public void testFireballAllowsSingleBounce() {
        BubbleGrid grid = new BubbleGrid();
        grid.setDimensions(0f, 100f, 40f);

        float launcherX = 400f;
        float launcherY = 1000f;
        // Moderate upward angle to right wall that reaches ceiling after 1 bounce
        float angleRad = (float) (-Math.PI * 0.65);

        TrajectoryCalculator.TrajectoryResult result = TrajectoryCalculator.calculateTrajectory(
                launcherX, launcherY, angleRad,
                0f, 800f, 100f,
                grid, 40f, true, 1
        );

        assertFalse(result.bounceLimitExceeded);
        assertFalse(result.points.isEmpty());
    }

    @Test
    public void testFireballBlocksSecondBounce() {
        BubbleGrid grid = new BubbleGrid();
        grid.setDimensions(0f, 100f, 40f);

        float launcherX = 400f;
        float launcherY = 1500f;
        // Very shallow angle that would hit right wall, then left wall before ceiling
        float angleRad = -0.25f;

        TrajectoryCalculator.TrajectoryResult result = TrajectoryCalculator.calculateTrajectory(
                launcherX, launcherY, angleRad,
                0f, 800f, 100f,
                grid, 40f, true, 1
        );

        assertTrue("Expected bounceLimitExceeded to be true for fireball attempting 2nd bounce", result.bounceLimitExceeded);
        assertFalse(result.points.isEmpty());
        PointF lastPoint = result.points.get(result.points.size() - 1);
        // The last point must stop exactly at the left wall boundary minX (40f)
        assertEquals(40f, lastPoint.x, 1.0f);
    }
}
