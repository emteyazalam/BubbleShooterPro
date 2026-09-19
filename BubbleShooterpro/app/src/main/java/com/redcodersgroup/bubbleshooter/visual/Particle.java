package com.redcodersgroup.bubbleshooter.visual;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class Particle {
    public enum ParticleShape {
        CIRCLE,
        STAR,
        CONFETTI
    }

    private float x;
    private float y;
    private float vx;
    private float vy;
    private float gravity = 800f;
    private float rotation = 0f;
    private float rotationSpeed = 0f;
    private float size;
    private int color;
    private float life = 1.0f;
    private float maxLife = 1.0f;
    private ParticleShape shape = ParticleShape.CIRCLE;

    private static final Path starPath = new Path();

    static {
        // Pre-compute normalized 5-point star path
        float outerR = 1.0f;
        float innerR = 0.42f;
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5 - Math.PI / 2;
            float r = (i % 2 == 0) ? outerR : innerR;
            float px = (float) (r * Math.cos(angle));
            float py = (float) (r * Math.sin(angle));
            if (i == 0) {
                starPath.moveTo(px, py);
            } else {
                starPath.lineTo(px, py);
            }
        }
        starPath.close();
    }

    public Particle(float x, float y, float vx, float vy, float size, int color, float maxLife, ParticleShape shape) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        this.color = color;
        this.maxLife = maxLife;
        this.life = maxLife;
        this.shape = shape;
        this.rotationSpeed = (float) ((Math.random() - 0.5) * 720.0);
    }

    public boolean isAlive() {
        return life > 0;
    }

    public void update(float dt) {
        vy += gravity * dt;
        x += vx * dt;
        y += vy * dt;
        rotation += rotationSpeed * dt;
        life -= dt;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (!isAlive()) return;

        float alphaRatio = Math.max(0f, life / maxLife);
        int currentAlpha = (int) (255 * alphaRatio);

        paint.setColor(color);
        paint.setAlpha(currentAlpha);
        paint.setStyle(Paint.Style.FILL);

        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(rotation);

        if (shape == ParticleShape.STAR) {
            canvas.scale(size, size);
            canvas.drawPath(starPath, paint);
        } else if (shape == ParticleShape.CONFETTI) {
            canvas.drawRect(-size, -size * 0.4f, size, size * 0.4f, paint);
        } else {
            canvas.drawCircle(0, 0, size * (0.3f + 0.7f * alphaRatio), paint);
        }

        canvas.restore();
    }
}
