package com.redcodersgroup.bubbleshooter.ui.splash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashFloatingBubblesView extends View {

    private static class AmbientBubble {
        float x, y;
        float radius;
        float speedY;
        float swayAmplitude;
        float swayFrequency;
        float swayOffset;
        float alpha;
        int colorTint;

        AmbientBubble(float x, float y, float radius, float speedY, float swayAmplitude, float swayFrequency, float alpha, int colorTint) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speedY = speedY;
            this.swayAmplitude = swayAmplitude;
            this.swayFrequency = swayFrequency;
            this.swayOffset = (float) (Math.random() * Math.PI * 2.0);
            this.alpha = alpha;
            this.colorTint = colorTint;
        }
    }

    private static class TwinkleStar {
        float x, y;
        float maxRadius;
        float phase;
        float speed;

        TwinkleStar(float x, float y, float maxRadius, float speed) {
            this.x = x;
            this.y = y;
            this.maxRadius = maxRadius;
            this.speed = speed;
            this.phase = (float) (Math.random() * Math.PI * 2.0);
        }
    }

    private final List<AmbientBubble> bubbles = new ArrayList<>();
    private final List<TwinkleStar> stars = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private long lastTimeNanos = 0;
    private float totalTime = 0f;

    public SplashFloatingBubblesView(Context context) {
        super(context);
        init();
    }

    public SplashFloatingBubblesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SplashFloatingBubblesView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setDither(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && bubbles.isEmpty()) {
            initElements(w, h);
        }
    }

    private void initElements(int w, int h) {
        bubbles.clear();
        stars.clear();

        int[] tints = {
                Color.parseColor("#38BDF8"), // Sky Blue
                Color.parseColor("#F472B6"), // Rose Pink
                Color.parseColor("#FBBF24"), // Amber Gold
                Color.parseColor("#4ADE80"), // Emerald
                Color.parseColor("#C084FC")  // Violet
        };

        // 18 ambient floating translucent bubbles
        for (int i = 0; i < 18; i++) {
            float r = 18f + random.nextFloat() * 32f;
            float x = random.nextFloat() * w;
            float y = random.nextFloat() * h;
            float speedY = 40f + random.nextFloat() * 65f;
            float swayAmp = 15f + random.nextFloat() * 25f;
            float swayFreq = 1.2f + random.nextFloat() * 1.5f;
            float alpha = 0.35f + random.nextFloat() * 0.40f;
            int tint = tints[i % tints.length];
            bubbles.add(new AmbientBubble(x, y, r, speedY, swayAmp, swayFreq, alpha, tint));
        }

        // 25 twinkling magic stars
        for (int i = 0; i < 25; i++) {
            float x = random.nextFloat() * w;
            float y = random.nextFloat() * h;
            float maxR = 3f + random.nextFloat() * 4.5f;
            float speed = 2.0f + random.nextFloat() * 3.0f;
            stars.add(new TwinkleStar(x, y, maxR, speed));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.nanoTime();
        if (lastTimeNanos == 0) lastTimeNanos = now;
        float dt = (now - lastTimeNanos) / 1_000_000_000.0f;
        if (dt > 0.05f) dt = 0.05f;
        lastTimeNanos = now;
        totalTime += dt;

        int w = getWidth();
        int h = getHeight();

        // 1. Draw and update twinkling stars
        for (TwinkleStar star : stars) {
            star.phase += dt * star.speed;
            float intensity = (float) (Math.sin(star.phase) * 0.5 + 0.5);
            if (intensity > 0.05f) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.WHITE);
                paint.setAlpha((int) (255 * intensity * 0.85f));
                drawDiamondStar(canvas, star.x, star.y, star.maxRadius * intensity);
            }
        }

        // 2. Draw and update bubbles
        for (AmbientBubble b : bubbles) {
            b.y -= b.speedY * dt;
            if (b.y < -b.radius * 2) {
                b.y = h + b.radius * 2;
                b.x = random.nextFloat() * w;
            }

            float currentX = b.x + (float) Math.sin(totalTime * b.swayFrequency + b.swayOffset) * b.swayAmplitude;
            drawSoapBubble(canvas, currentX, b.y, b.radius, b.alpha, b.colorTint);
        }

        postInvalidateOnAnimation();
    }

    private void drawDiamondStar(Canvas canvas, float cx, float cy, float r) {
        canvas.save();
        canvas.translate(cx, cy);

        // Core glow
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, r * 0.6f, paint);

        // 4-point spikes
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(0, -r * 2.2f);
        path.lineTo(r * 0.4f, 0);
        path.lineTo(0, r * 2.2f);
        path.lineTo(-r * 0.4f, 0);
        path.close();

        path.moveTo(-r * 2.2f, 0);
        path.lineTo(0, r * 0.4f);
        path.lineTo(r * 2.2f, 0);
        path.lineTo(0, -r * 0.4f);
        path.close();

        canvas.drawPath(path, paint);
        canvas.restore();
    }

    private void drawSoapBubble(Canvas canvas, float cx, float cy, float r, float alpha, int tint) {
        canvas.save();
        canvas.translate(cx, cy);

        // Outer glow/translucent rim
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.5f, r * 0.08f));
        paint.setColor(tint);
        paint.setAlpha((int) (180 * alpha));
        canvas.drawCircle(0, 0, r, paint);

        // Inner spherical gradient
        paint.setStyle(Paint.Style.FILL);
        RadialGradient grad = new RadialGradient(
                -r * 0.3f, -r * 0.3f, r * 1.2f,
                new int[]{Color.argb((int) (40 * alpha), 255, 255, 255), Color.argb((int) (90 * alpha), Color.red(tint), Color.green(tint), Color.blue(tint)), Color.TRANSPARENT},
                new float[]{0.0f, 0.7f, 1.0f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(grad);
        canvas.drawCircle(0, 0, r, paint);
        paint.setShader(null);

        // Specular highlight crescent/oval on top left
        paint.setColor(Color.WHITE);
        paint.setAlpha((int) (220 * alpha));
        canvas.save();
        canvas.translate(-r * 0.32f, -r * 0.32f);
        canvas.rotate(-35f);
        canvas.drawOval(-r * 0.28f, -r * 0.12f, r * 0.28f, r * 0.12f, paint);
        canvas.restore();

        // Pinpoint reflection at bottom right
        paint.setAlpha((int) (160 * alpha));
        canvas.drawCircle(r * 0.35f, r * 0.35f, Math.max(1f, r * 0.08f), paint);

        canvas.restore();
    }
}
