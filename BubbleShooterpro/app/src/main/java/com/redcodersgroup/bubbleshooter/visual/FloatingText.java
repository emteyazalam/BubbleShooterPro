package com.redcodersgroup.bubbleshooter.visual;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

public class FloatingText {
    private final String text;
    private float x;
    private float y;
    private final int color;
    private final float textSize;
    private float life;
    private final float maxLife;

    public FloatingText(String text, float x, float y, int color, float textSize, float duration) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.textSize = textSize;
        this.life = duration;
        this.maxLife = duration;
    }

    public boolean isAlive() {
        return life > 0;
    }

    public void update(float dt) {
        y -= 90f * dt; // Drifts upward
        life -= dt;
    }

    public void draw(Canvas canvas, Paint paint) {
        if (!isAlive()) return;

        float progress = 1.0f - (life / maxLife);
        float alphaRatio = Math.max(0f, Math.min(1f, life / (maxLife * 0.4f)));

        // Bouncy scale-up at beginning
        float scale = 1.0f;
        if (progress < 0.25f) {
            scale = 0.5f + (progress / 0.25f) * 0.7f; // pops to 1.2x
        } else if (progress < 0.45f) {
            scale = 1.2f - ((progress - 0.25f) / 0.2f) * 0.2f; // settles to 1.0x
        }

        canvas.save();
        canvas.translate(x, y);
        canvas.scale(scale, scale);

        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);

        // 1. Text drop shadow/stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(textSize * 0.15f);
        paint.setColor(Color.argb((int) (220 * alphaRatio), 0, 0, 0));
        canvas.drawText(text, 0, 0, paint);

        // 2. Main fill text
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAlpha((int) (255 * alphaRatio));
        canvas.drawText(text, 0, 0, paint);

        canvas.restore();
    }
}
