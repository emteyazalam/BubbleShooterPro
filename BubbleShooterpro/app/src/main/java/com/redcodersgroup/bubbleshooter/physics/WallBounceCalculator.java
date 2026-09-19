package com.redcodersgroup.bubbleshooter.physics;

import com.redcodersgroup.bubbleshooter.bubble.BubbleProjectile;

public class WallBounceCalculator {

    /**
     * Checks if the projectile has collided with the left or right wall,
     * clamps its position within boundaries and reverses its horizontal velocity.
     * Returns true if a bounce occurred.
     */
    public static boolean checkAndHandleWallBounce(BubbleProjectile projectile, float leftBound, float rightBound) {
        if (projectile == null || !projectile.isActive()) return false;

        float r = projectile.getRadius();
        float minX = leftBound + r;
        float maxX = rightBound - r;

        if (projectile.getX() <= minX && projectile.getVx() < 0) {
            projectile.setX(minX);
            projectile.setVx(-projectile.getVx());
            return true;
        } else if (projectile.getX() >= maxX && projectile.getVx() > 0) {
            projectile.setX(maxX);
            projectile.setVx(-projectile.getVx());
            return true;
        }

        return false;
    }
}
