package com.redcodersgroup.bubbleshooter.physics;

import com.redcodersgroup.bubbleshooter.board.BubbleBoard;
import com.redcodersgroup.bubbleshooter.board.BubbleGrid;
import com.redcodersgroup.bubbleshooter.board.GridPosition;
import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import com.redcodersgroup.bubbleshooter.bubble.BubbleProjectile;

public class CollisionDetector {

    public static class CollisionResult {
        public final boolean collided;
        public final GridPosition snapPosition;

        public CollisionResult(boolean collided, GridPosition snapPosition) {
            this.collided = collided;
            this.snapPosition = snapPosition;
        }
    }

    public static CollisionResult checkCollision(BubbleProjectile projectile, BubbleBoard board, float topBound) {
        if (projectile == null || !projectile.isActive() || board == null) {
            return new CollisionResult(false, null);
        }

        float r = projectile.getRadius();
        float px = projectile.getX();
        float py = projectile.getY();

        // 1. Check Ceiling Collision
        if (py - r <= topBound) {
            GridPosition snapPos = board.findNearestSnapPosition(px, topBound + r);
            return new CollisionResult(true, snapPos);
        }

        // 2. Check Collision with Board Bubbles
        BubbleGrid grid = board.getGrid();
        float thresholdDistSq = (r * 2f * 0.92f) * (r * 2f * 0.92f);

        for (Bubble b : grid.getAllBubbles()) {
            if (b != null && !b.isPopping() && !b.isFalling()) {
                float distSq = (px - b.getX()) * (px - b.getX()) + (py - b.getY()) * (py - b.getY());
                if (distSq <= thresholdDistSq) {
                    GridPosition snapPos = board.findNearestSnapPosition(px, py);
                    return new CollisionResult(true, snapPos);
                }
            }
        }

        return new CollisionResult(false, null);
    }
}
