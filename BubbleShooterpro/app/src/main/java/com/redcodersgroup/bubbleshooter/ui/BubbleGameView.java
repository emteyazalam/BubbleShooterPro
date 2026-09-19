package com.redcodersgroup.bubbleshooter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import com.redcodersgroup.bubbleshooter.game.GameEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BubbleGameView extends View {

    public enum BiomeTheme {
        MEADOWS("Bubble Meadows",
                new int[]{0xFF0E381E, 0xFF14532D, 0xFF166534},
                0xFF0F4724, 0xFF4ADE80, 0xFF22C55E, "pollen"),
        CRYSTALS("Crystal Caverns",
                new int[]{0xFF1A0933, 0xFF2E1065, 0xFF4C1D95},
                0xFF2A0F54, 0xFFC084FC, 0xFFA855F7, "crystal"),
        COSMOS("Celestial Cosmos",
                new int[]{0xFF0B0F2B, 0xFF1E1B4B, 0xFF312E81},
                0xFF171738, 0xFF818CF8, 0xFF6366F1, "stars"),
        VOLCANO("Volcanic Forge",
                new int[]{0xFF2B0A0A, 0xFF450A0A, 0xFF7F1D1D},
                0xFF3B0B0B, 0xFFFB923C, 0xFFEF4444, "embers"),
        CYBER("Neon Cyberland",
                new int[]{0xFF04202C, 0xFF0F172A, 0xFF2E1065},
                0xFF083344, 0xFF22D3EE, 0xFF06B6D4, "cyber"),
        ATLANTIS("Sunken Atlantis",
                new int[]{0xFF022B42, 0xFF075985, 0xFF0369A1},
                0xFF034164, 0xFF38BDF8, 0xFF0EA5E9, "bubbles"),
        JUNGLE("Enchanted Jungle",
                new int[]{0xFF022C1A, 0xFF064E3B, 0xFF14532D},
                0xFF064227, 0xFFA7F3D0, 0xFF10B981, "fireflies"),
        GLACIER("Frozen Glacier",
                new int[]{0xFF082F49, 0xFF0C4A6E, 0xFF0284C7},
                0xFF0A3B5C, 0xFFBAE6FD, 0xFF38BDF8, "snow"),
        DESERT("Desert Mirage",
                new int[]{0xFF3B1A04, 0xFF582B08, 0xFF78350F},
                0xFF4A2207, 0xFFFDE047, 0xFFF59E0B, "sand"),
        THUNDER("Thunder Peak",
                new int[]{0xFF1E1035, 0xFF3B0764, 0xFF4C1D95},
                0xFF2B1047, 0xFFFACC15, 0xFFA855F7, "lightning"),
        INFINITY("Infinity Realm",
                new int[]{0xFF180A2E, 0xFF2E1065, 0xFF831843},
                0xFF250E42, 0xFFF472B6, 0xFFEC4899, "astral");

        public final String title;
        public final int[] gradientColors;
        public final int ceilingColor;
        public final int railColor;
        public final int particleColor;
        public final String particleType;

        BiomeTheme(String title, int[] gradientColors, int ceilingColor, int railColor, int particleColor, String particleType) {
            this.title = title;
            this.gradientColors = gradientColors;
            this.ceilingColor = ceilingColor;
            this.railColor = railColor;
            this.particleColor = particleColor;
            this.particleType = particleType;
        }

        public static BiomeTheme forLevel(int level) {
            if (level <= 30) return MEADOWS;
            if (level <= 60) return CRYSTALS;
            if (level <= 90) return COSMOS;
            if (level <= 120) return VOLCANO;
            if (level <= 150) return CYBER;
            if (level <= 180) return ATLANTIS;
            if (level <= 210) return JUNGLE;
            if (level <= 240) return GLACIER;
            if (level <= 270) return DESERT;
            if (level <= 300) return THUNDER;
            return INFINITY;
        }
    }

    private static class AmbientParticle {
        float x, y;
        float vx, vy;
        float radius;
        float alpha;
        float alphaSpeed;
        float wobblePhase;
        float wobbleSpeed;
        int color;
    }

    private GameEngine gameEngine;
    private Paint paint;
    private Paint particlePaint;
    private long lastTimeNanos = 0;
    private LinearGradient backgroundGradient;
    private BiomeTheme currentBiome = BiomeTheme.MEADOWS;
    private int currentLevel = 1;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private boolean isViewPaused = false;
    private final List<AmbientParticle> particles = new ArrayList<>();
    private final Random random = new Random();

    public BubbleGameView(Context context) {
        super(context);
        init();
    }

    public BubbleGameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleGameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        this.paint.setDither(true);
        this.particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bubble.initResources(getContext());
    }

    public void setBiomeLevel(int levelNumber) {
        this.currentLevel = levelNumber;
        this.currentBiome = BiomeTheme.forLevel(levelNumber);
        updateBackgroundGradient();
        initParticles();
        invalidate();
    }

    public BiomeTheme getCurrentBiome() {
        return currentBiome;
    }

    public void pause() {
        this.isViewPaused = true;
        this.lastTimeNanos = 0;
    }

    public void resume() {
        this.isViewPaused = false;
        this.lastTimeNanos = 0;
        postInvalidateOnAnimation();
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        if (viewWidth > 0 && viewHeight > 0) {
            this.gameEngine.setViewBounds(viewWidth, viewHeight);
        }
        invalidate();
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    private void updateBackgroundGradient() {
        if (viewWidth > 0 && viewHeight > 0 && currentBiome != null) {
            this.backgroundGradient = new LinearGradient(
                    0, 0, 0, viewHeight,
                    currentBiome.gradientColors,
                    new float[]{0.0f, 0.55f, 1.0f},
                    Shader.TileMode.CLAMP
            );
        }
    }

    private void initParticles() {
        particles.clear();
        if (viewWidth <= 0 || viewHeight <= 0) return;

        int particleCount = 22;
        for (int i = 0; i < particleCount; i++) {
            AmbientParticle p = new AmbientParticle();
            resetParticle(p, true);
            particles.add(p);
        }
    }

    private void resetParticle(AmbientParticle p, boolean randomY) {
        p.x = random.nextFloat() * (viewWidth > 0 ? viewWidth : 1080);
        p.y = randomY ? (random.nextFloat() * (viewHeight > 0 ? viewHeight : 1920)) : (viewHeight + 10f);
        p.alpha = 0.2f + random.nextFloat() * 0.6f;
        p.alphaSpeed = 0.5f + random.nextFloat() * 1.5f;
        p.wobblePhase = random.nextFloat() * 6.28f;
        p.wobbleSpeed = 1.0f + random.nextFloat() * 2.0f;
        p.color = currentBiome.particleColor;

        if ("embers".equals(currentBiome.particleType)) {
            p.radius = 2.5f + random.nextFloat() * 4.5f;
            p.vx = (random.nextFloat() - 0.5f) * 20f;
            p.vy = -35f - random.nextFloat() * 55f; // Fast rising embers
        } else if ("bubbles".equals(currentBiome.particleType)) {
            p.radius = 4.0f + random.nextFloat() * 8.0f;
            p.vx = (random.nextFloat() - 0.5f) * 15f;
            p.vy = -20f - random.nextFloat() * 35f; // Gentle rising aquatic bubbles
        } else if ("snow".equals(currentBiome.particleType)) {
            p.radius = 2.5f + random.nextFloat() * 4.0f;
            p.vx = (random.nextFloat() - 0.5f) * 25f;
            p.vy = 25f + random.nextFloat() * 40f; // Falling snowflakes
            if (!randomY) p.y = -10f;
        } else if ("fireflies".equals(currentBiome.particleType) || "pollen".equals(currentBiome.particleType)) {
            p.radius = 3.0f + random.nextFloat() * 5.0f;
            p.vx = (random.nextFloat() - 0.5f) * 15f;
            p.vy = -10f - random.nextFloat() * 20f;
        } else if ("cyber".equals(currentBiome.particleType) || "lightning".equals(currentBiome.particleType)) {
            p.radius = 2.0f + random.nextFloat() * 4.0f;
            p.vx = (random.nextFloat() - 0.5f) * 40f;
            p.vy = -20f - random.nextFloat() * 40f;
        } else {
            // Stars / Crystal glints / Astral stardust
            p.radius = 2.5f + random.nextFloat() * 5.0f;
            p.vx = (random.nextFloat() - 0.5f) * 10f;
            p.vy = -8f - random.nextFloat() * 15f;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.viewWidth = w;
        this.viewHeight = h;

        updateBackgroundGradient();
        initParticles();

        if (gameEngine != null) {
            gameEngine.setViewBounds(w, h);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameEngine == null) return super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                gameEngine.onTouchDown(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                gameEngine.onTouchMove(x, y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                gameEngine.onTouchUp(x, y);
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.nanoTime();
        if (lastTimeNanos == 0) {
            lastTimeNanos = now;
        }
        float dt = (now - lastTimeNanos) / 1_000_000_000.0f;
        lastTimeNanos = now;

        // Clamp delta time to avoid physics explosion if paused/backgrounded
        if (dt > 0.05f) dt = 0.05f;

        // 1. Draw Biome-Themed Background Gradient
        if (backgroundGradient != null) {
            paint.setShader(backgroundGradient);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            paint.setShader(null);
        } else {
            canvas.drawColor(currentBiome.gradientColors[0]);
        }

        // 2. Draw Ambient Background Atmosphere Particles
        if (!isViewPaused) {
            updateAndDrawParticles(canvas, dt);
        } else {
            drawParticles(canvas);
        }

        // 3. Draw Biome Ceiling & Gold Accent Rail (Cleanly positioned under top HUD)
        float topY = (gameEngine != null) ? gameEngine.getBoardTop() : (92f * getResources().getDisplayMetrics().density);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentBiome.ceilingColor);
        canvas.drawRect(0, 0, getWidth(), topY, paint);

        // Ceiling accent rail
        paint.setColor(currentBiome.railColor);
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, topY, getWidth(), topY, paint);
        paint.setStyle(Paint.Style.FILL);

        // 3.5 Tablet & Wide Screen Boundaries (Elegant side rails and vignette framing)
        if (gameEngine != null && gameEngine.getBoardLeft() > 0) {
            float bLeft = gameEngine.getBoardLeft();
            float bRight = gameEngine.getBoardRight();

            // Side pillar subtle vignette shade
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(75, 0, 0, 0));
            canvas.drawRect(0, topY, bLeft, getHeight(), paint);
            canvas.drawRect(bRight, topY, getWidth(), getHeight(), paint);

            // Left and Right boundary accent rails
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4.5f);
            paint.setColor(currentBiome.railColor);
            canvas.drawLine(bLeft, topY, bLeft, getHeight(), paint);
            canvas.drawLine(bRight, topY, bRight, getHeight(), paint);
            paint.setStyle(Paint.Style.FILL);
        }

        // 4. Update and Draw Game Engine
        if (gameEngine != null) {
            if (!isViewPaused) {
                gameEngine.update(dt);
            }
            gameEngine.draw(canvas, paint);
        }

        // 5. Continuously request redraw for 60 FPS smooth animations
        if (!isViewPaused) {
            postInvalidateOnAnimation();
        }
    }

    private void updateAndDrawParticles(Canvas canvas, float dt) {
        for (AmbientParticle p : particles) {
            p.wobblePhase += p.wobbleSpeed * dt;
            float wobbleX = (float) Math.sin(p.wobblePhase) * 12f * dt;
            p.x += (p.vx * dt) + wobbleX;
            p.y += p.vy * dt;

            // Pulse alpha
            p.alpha += (float) Math.sin(p.wobblePhase) * 0.15f * dt;
            if (p.alpha < 0.15f) p.alpha = 0.15f;
            if (p.alpha > 0.85f) p.alpha = 0.85f;

            // Out of bounds reset
            if (p.y < -20 || p.y > viewHeight + 30 || p.x < -20 || p.x > viewWidth + 20) {
                resetParticle(p, false);
            }

            drawSingleParticle(canvas, p);
        }
    }

    private void drawParticles(Canvas canvas) {
        for (AmbientParticle p : particles) {
            drawSingleParticle(canvas, p);
        }
    }

    private void drawSingleParticle(Canvas canvas, AmbientParticle p) {
        particlePaint.setColor(p.color);
        particlePaint.setAlpha((int) (p.alpha * 255));

        if ("bubbles".equals(currentBiome.particleType)) {
            particlePaint.setStyle(Paint.Style.STROKE);
            particlePaint.setStrokeWidth(1.8f);
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);

            // Bubble shine dot
            particlePaint.setStyle(Paint.Style.FILL);
            particlePaint.setColor(Color.WHITE);
            particlePaint.setAlpha((int) (p.alpha * 180));
            canvas.drawCircle(p.x - p.radius * 0.35f, p.y - p.radius * 0.35f, p.radius * 0.3f, particlePaint);
        } else {
            particlePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
        }
    }
}
