package com.redcodersgroup.bubbleshooter.ui.splash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.bubble.BubbleColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashAnimationView extends View {

    public interface OnSplashFinishedListener {
        void onSplashFinished();
    }

    private static class SplashBubble {
        float x, y;
        float vx, vy;
        float radius;
        BubbleColor color;
        float alpha = 1.0f;
        boolean scattered = false;

        SplashBubble(float x, float y, float radius, BubbleColor color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
        }
    }

    private static class SplashParticle {
        float x, y;
        float vx, vy;
        int color;
        float radius;
        float alpha = 1.0f;
        float decay;

        SplashParticle(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
            Random r = new Random();
            float angle = (float) (r.nextDouble() * Math.PI * 2.0);
            float speed = 200f + r.nextFloat() * 600f;
            this.vx = (float) Math.cos(angle) * speed;
            this.vy = (float) Math.sin(angle) * speed;
            this.radius = 4f + r.nextFloat() * 8f;
            this.decay = 1.2f + r.nextFloat() * 1.5f;
        }

        void update(float dt) {
            x += vx * dt;
            y += vy * dt;
            vy += 450f * dt; // gravity
            alpha -= decay * dt;
        }
    }

    private Paint paint;
    private LinearGradient backgroundGradient;
    private SoundManager soundManager;
    private OnSplashFinishedListener listener;

    private final List<SplashBubble> clusterBubbles = new ArrayList<>();
    private final List<SplashParticle> particles = new ArrayList<>();
    private SplashBubble projectileBubble;

    private float elapsedTime = 0f;
    private long lastTimeNanos = 0;
    private boolean soundPlayed = false;
    private boolean finishedTriggered = false;

    private float titleScale = 0.0f;
    private float titleAlpha = 0.0f;

    public SplashAnimationView(Context context) {
        super(context);
        init();
    }

    public SplashAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SplashAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        soundManager = SoundManager.getInstance(getContext());
    }

    public void setOnSplashFinishedListener(OnSplashFinishedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        backgroundGradient = new LinearGradient(
                0, 0, 0, h,
                new int[]{
                        Color.parseColor("#12162E"),
                        Color.parseColor("#1B1F3B"),
                        Color.parseColor("#273469")
                },
                new float[]{0.0f, 0.5f, 1.0f},
                Shader.TileMode.CLAMP
        );

        initCluster(w, h);
    }

    private void initCluster(int w, int h) {
        clusterBubbles.clear();
        particles.clear();

        float r = w / 16f; // Standard bubble radius
        float centerX = w * 0.5f;
        float clusterCenterY = h * 0.38f;

        // Create a circular cluster of 7 colorful bubbles
        BubbleColor[] colors = {
                BubbleColor.PURPLE, BubbleColor.BLUE, BubbleColor.YELLOW,
                BubbleColor.RED, BubbleColor.GREEN, BubbleColor.CYAN,
                BubbleColor.ORANGE
        };

        // Center bubble
        clusterBubbles.add(new SplashBubble(centerX, clusterCenterY, r * 1.05f, colors[0]));

        // 6 Surrounding bubbles
        for (int i = 0; i < 6; i++) {
            double angle = (i * Math.PI / 3.0);
            float bx = (float) (centerX + Math.cos(angle) * (r * 2.0f));
            float by = (float) (clusterCenterY + Math.sin(angle) * (r * 2.0f));
            clusterBubbles.add(new SplashBubble(bx, by, r * 1.05f, colors[(i + 1) % colors.length]));
        }

        // Projectile Bubble placed at the bottom center launcher
        float launcherY = h * 0.85f;
        projectileBubble = new SplashBubble(centerX, launcherY, r * 1.1f, BubbleColor.RAINBOW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.nanoTime();
        if (lastTimeNanos == 0) lastTimeNanos = now;
        float dt = (now - lastTimeNanos) / 1_000_000_000.0f;
        if (dt > 0.05f) dt = 0.05f;
        lastTimeNanos = now;

        elapsedTime += dt;

        int w = getWidth();
        int h = getHeight();

        // 1. Draw Background
        if (backgroundGradient != null) {
            paint.setShader(backgroundGradient);
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
        } else {
            canvas.drawColor(Color.parseColor("#1B1F3B"));
        }

        float clusterCenterY = h * 0.38f;
        float centerX = w * 0.5f;

        // Stage 1: Float before shot (0.0s - 0.4s)
        // Stage 2: Auto-Shoot! (0.4s - 0.85s)
        if (elapsedTime > 0.4f && elapsedTime <= 0.85f) {
            float shootProgress = (elapsedTime - 0.4f) / 0.45f;
            float startY = h * 0.85f;
            projectileBubble.y = startY - shootProgress * (startY - clusterCenterY);
        }

        // Stage 3: Impact, Scatter & Confetti Burst! (0.85s+)
        if (elapsedTime > 0.85f) {
            if (!soundPlayed) {
                soundPlayed = true;
                soundManager.playPop(3);
                soundManager.playWin();

                // Explode cluster bubbles outwards
                Random rand = new Random();
                for (SplashBubble b : clusterBubbles) {
                    b.scattered = true;
                    float dx = b.x - centerX;
                    float dy = b.y - clusterCenterY;
                    double angle = Math.atan2(dy, dx) + (rand.nextFloat() - 0.5) * 0.5;
                    float speed = 400f + rand.nextFloat() * 500f;
                    b.vx = (float) Math.cos(angle) * speed;
                    b.vy = (float) Math.sin(angle) * speed - 150f;
                }

                // Projectile pops into particles
                projectileBubble.alpha = 0f;

                // Spawn 60 celebratory particles
                int[] particleColors = {
                        0xFFFFD600, 0xFFFF1744, 0xFF00C853,
                        0xFF0091EA, 0xFFAA00FF, 0xFF00E5FF, 0xFFFFFFFF
                };
                for (int i = 0; i < 60; i++) {
                    particles.add(new SplashParticle(centerX, clusterCenterY, particleColors[i % particleColors.length]));
                }
            }

            // Update scattered bubbles
            for (SplashBubble b : clusterBubbles) {
                if (b.scattered) {
                    b.x += b.vx * dt;
                    b.y += b.vy * dt;
                    b.vy += 850f * dt; // gravity
                    b.alpha = Math.max(0f, b.alpha - dt * 0.8f);
                }
            }

            // Update particles
            for (SplashParticle p : particles) {
                p.update(dt);
            }

            // Title zoom animation
            float titleTime = elapsedTime - 0.85f;
            if (titleTime < 0.6f) {
                float t = titleTime / 0.6f;
                // Elastic / Overshoot curve
                titleScale = (float) (Math.sin(-13 * (t + 1) * Math.PI / 2) * Math.pow(2, -10 * t) + 1);
                titleAlpha = Math.min(1.0f, t * 2.0f);
            } else {
                titleScale = 1.0f;
                titleAlpha = 1.0f;
            }
        }

        // Draw Cluster Bubbles
        for (SplashBubble b : clusterBubbles) {
            drawBubble(canvas, b);
        }

        // Draw Projectile Bubble
        if (projectileBubble != null && projectileBubble.alpha > 0f) {
            drawBubble(canvas, projectileBubble);
        }

        // Draw Particles
        for (SplashParticle p : particles) {
            if (p.alpha > 0f) {
                paint.setColor(p.color);
                paint.setAlpha((int) (255 * p.alpha));
                canvas.drawCircle(p.x, p.y, p.radius, paint);
            }
        }

        // Draw 3D Title when Impact occurs
        if (titleAlpha > 0f) {
            canvas.save();
            canvas.translate(centerX, clusterCenterY);
            canvas.scale(titleScale, titleScale);

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAlpha((int) (255 * titleAlpha));

            // Title Shadow
            paint.setColor(Color.parseColor("#78350F"));
            paint.setTextSize(w * 0.11f);
            paint.setFakeBoldText(true);
            canvas.drawText("BUBBLE", 0, -w * 0.05f + 6, paint);
            canvas.drawText("SHOOTER", 0, w * 0.09f + 6, paint);

            // Title Main Text
            paint.setColor(Color.parseColor("#FDE047"));
            canvas.drawText("BUBBLE", 0, -w * 0.05f, paint);
            canvas.drawText("SHOOTER", 0, w * 0.09f, paint);

            // Pro Edition Subtitle
            paint.setColor(Color.parseColor("#FF3366"));
            paint.setTextSize(w * 0.045f);
            canvas.drawText("✦ PRO EDITION ✦", 0, w * 0.20f, paint);

            canvas.restore();
        }

        // Stage 4: Trigger Finish at 2.4 seconds
        if (elapsedTime >= 2.4f && !finishedTriggered) {
            finishedTriggered = true;
            if (listener != null) {
                listener.onSplashFinished();
            }
        }

        if (elapsedTime < 3.0f) {
            postInvalidateOnAnimation();
        }
    }

    private void drawBubble(Canvas canvas, SplashBubble b) {
        if (b.alpha <= 0f || b.radius <= 0f) return;

        canvas.save();
        canvas.translate(b.x, b.y);

        float r = b.radius;
        BubbleColor col = (b.color != null) ? b.color : BubbleColor.RED;

        // 1. Subtle drop shadow
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb((int) (60 * b.alpha), 0, 0, 0));
        canvas.drawCircle(0, r * 0.12f, r, paint);

        // 2. Base vibrant solid
        paint.setColor(col.primaryColor);
        paint.setAlpha((int) (255 * b.alpha));
        canvas.drawCircle(0, 0, r, paint);

        // 3. 3D Spherical bevel gradient
        float lightOffset = r * 0.25f;
        RadialGradient sphereGradient = new RadialGradient(
                -lightOffset, -lightOffset, r * 1.3f,
                new int[]{Color.TRANSPARENT, Color.TRANSPARENT, col.darkColor},
                new float[]{0.0f, 0.65f, 1.0f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(sphereGradient);
        canvas.drawCircle(0, 0, r, paint);
        paint.setShader(null);

        // 4. Outer rim stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(r * 0.08f);
        paint.setColor(col.darkColor);
        paint.setAlpha((int) (180 * b.alpha));
        canvas.drawCircle(0, 0, r * 0.96f, paint);
        paint.setStyle(Paint.Style.FILL);

        // 5. Specular highlight
        paint.setColor(Color.WHITE);
        paint.setAlpha((int) (200 * b.alpha));
        canvas.save();
        canvas.translate(-r * 0.35f, -r * 0.35f);
        canvas.rotate(-40f);
        canvas.drawOval(-r * 0.26f, -r * 0.13f, r * 0.26f, r * 0.13f, paint);
        canvas.restore();

        // 6. Pinpoint sparkle
        paint.setAlpha((int) (240 * b.alpha));
        canvas.drawCircle(-r * 0.22f, -r * 0.52f, r * 0.07f, paint);

        canvas.restore();
    }
}
