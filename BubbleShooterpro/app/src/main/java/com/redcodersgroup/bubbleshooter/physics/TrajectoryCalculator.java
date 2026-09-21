package com.redcodersgroup.bubbleshooter.physics;

import android.graphics.PointF;
import com.redcodersgroup.bubbleshooter.board.BubbleGrid;
import com.redcodersgroup.bubbleshooter.board.GridPosition;
import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryCalculator {
    public static final float STEP_SIZE = 22f; // px between guide points
    public static final int MAX_STEPS = 120;
    public static final int MAX_BOUNCES = 2;

    public static class TrajectoryResult {
        public final List<PointF> points;
        public final boolean bounceLimitExceeded;

        public TrajectoryResult(List<PointF> points, boolean bounceLimitExceeded) {
            this.points = points;
            this.bounceLimitExceeded = bounceLimitExceeded;
        }
    }

    public static List<PointF> calculateTrajectory(
            float startX, float startY, float angleRad,
            float leftBound, float rightBound, float topBound,
            BubbleGrid grid, float bubbleRadius) {
        return calculateTrajectory(startX, startY, angleRad, leftBound, rightBound, topBound, grid, bubbleRadius, false, MAX_BOUNCES).points;
    }

    public static List<PointF> calculateTrajectory(
            float startX, float startY, float angleRad,
            float leftBound, float rightBound, float topBound,
            BubbleGrid grid, float bubbleRadius, boolean isPiercing) {
        return calculateTrajectory(startX, startY, angleRad, leftBound, rightBound, topBound, grid, bubbleRadius, isPiercing, isPiercing ? 1 : MAX_BOUNCES).points;
    }

    public static TrajectoryResult calculateTrajectory(
            float startX, float startY, float angleRad,
            float leftBound, float rightBound, float topBound,
            BubbleGrid grid, float bubbleRadius, boolean isPiercing, int maxBounces) {

        List<PointF> points = new ArrayList<>();
        float minX = leftBound + bubbleRadius;
        float maxX = rightBound - bubbleRadius;
        float minY = topBound + bubbleRadius;

        float rx = startX;
        float ry = startY;
        float dx = (float) Math.cos(angleRad);
        float dy = (float) Math.sin(angleRad);

        // Normalize direction
        float len = (float) Math.hypot(dx, dy);
        if (len == 0) return new TrajectoryResult(points, false);
        dx /= len;
        dy /= len;

        int bounces = 0;
        boolean bounceLimitExceeded = false;
        float collisionDistSq = (bubbleRadius * 2f * 0.92f) * (bubbleRadius * 2f * 0.92f);

        for (int i = 0; i < MAX_STEPS; i++) {
            rx += dx * STEP_SIZE;
            ry += dy * STEP_SIZE;

            // 1. Wall bounce checks
            if (rx <= minX && dx < 0) {
                rx = minX;
                if (bounces >= maxBounces) {
                    bounceLimitExceeded = true;
                    points.add(new PointF(rx, ry));
                    break;
                }
                dx = -dx;
                bounces++;
            } else if (rx >= maxX && dx > 0) {
                rx = maxX;
                if (bounces >= maxBounces) {
                    bounceLimitExceeded = true;
                    points.add(new PointF(rx, ry));
                    break;
                }
                dx = -dx;
                bounces++;
            }

            // 2. Ceiling hit
            if (ry <= minY) {
                ry = minY;
                points.add(new PointF(rx, ry));
                break;
            }

            // 3. Collision with existing grid bubbles (ignored if piercing)
            boolean collided = false;
            if (!isPiercing && grid != null) {
                for (Bubble b : grid.getAllBubbles()) {
                    if (b != null && !b.isPopping() && !b.isFalling()) {
                        float distSq = (rx - b.getX()) * (rx - b.getX()) + (ry - b.getY()) * (ry - b.getY());
                        if (distSq <= collisionDistSq) {
                            collided = true;
                            break;
                        }
                    }
                }
            }

            points.add(new PointF(rx, ry));

            if (collided) {
                break;
            }
        }

        return new TrajectoryResult(points, bounceLimitExceeded);
    }
}
