package com.redcodersgroup.bubbleshooter.bubble;

import android.graphics.Canvas;
import android.graphics.Paint;

public class BubbleProjectile {
    private float x;
    private float y;
    private float vx;
    private float vy;
    private float radius;
    private float speed = 2500f; // px per second
    private BubbleColor color;
    private BubbleType type;
    private boolean active = false;
    private final Bubble renderBubble;

    public BubbleProjectile(BubbleColor color, BubbleType type, float radius) {
        this.color = color;
        this.type = type;
        this.radius = radius;
        this.renderBubble = new Bubble(color, type, null);
        this.renderBubble.setRadius(radius);
    }

    public void launch(float startX, float startY, float dirX, float dirY) {
        this.x = startX;
        this.y = startY;
        float length = (float) Math.hypot(dirX, dirY);
        if (length == 0) length = 1;
        this.vx = (dirX / length) * speed;
        this.vy = (dirY / length) * speed;
        this.active = true;

        this.renderBubble.setColor(color);
        this.renderBubble.setType(type);
        this.renderBubble.setRadius(radius);
        this.renderBubble.setX(x);
        this.renderBubble.setY(y);
    }

    public void update(float dt) {
        if (!active) return;
        x += vx * dt;
        y += vy * dt;
        renderBubble.setX(x);
        renderBubble.setY(y);
    }

    public void draw(Canvas canvas, Paint paint) {
        if (!active) return;
        renderBubble.draw(canvas, paint);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        renderBubble.setX(x);
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        renderBubble.setY(y);
    }

    public float getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public float getVy() {
        return vy;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        renderBubble.setRadius(radius);
    }

    public BubbleColor getColor() {
        return color;
    }

    public void setColor(BubbleColor color) {
        this.color = color;
        renderBubble.setColor(color);
    }

    public BubbleType getType() {
        return type;
    }

    public void setType(BubbleType type) {
        this.type = type;
        renderBubble.setType(type);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
