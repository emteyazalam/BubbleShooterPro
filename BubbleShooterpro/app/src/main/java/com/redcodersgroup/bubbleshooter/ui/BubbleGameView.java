package com.redcodersgroup.bubbleshooter.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import com.redcodersgroup.bubbleshooter.game.GameEngine;

public class BubbleGameView extends View {

    private GameEngine gameEngine;
    private Paint paint;
    private long lastTimeNanos = 0;
    private LinearGradient backgroundGradient;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private boolean isViewPaused = false;

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
        Bubble.initResources(getContext());
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.viewWidth = w;
        this.viewHeight = h;

        // Rich cartoon sky/space gradient
        this.backgroundGradient = new LinearGradient(
                0, 0, 0, h,
                new int[]{
                        Color.parseColor("#1B1F3B"), // Deep Midnight Indigo
                        Color.parseColor("#1E2749"), // Twilight Royal Blue
                        Color.parseColor("#273469")  // Cheerful Soft Navy
                },
                new float[]{0.0f, 0.55f, 1.0f},
                Shader.TileMode.CLAMP
        );

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

        // 1. Draw Playful Vibrant Background
        if (backgroundGradient != null) {
            paint.setShader(backgroundGradient);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            paint.setShader(null);
        } else {
            canvas.drawColor(Color.parseColor("#1B1F3B"));
        }

        // Draw Ceiling / Top Border Studs (Cleanly under top HUD)
        float topY = (gameEngine != null) ? gameEngine.getBoardTop() : (68f * getResources().getDisplayMetrics().density);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#2F3858"));
        canvas.drawRect(0, 0, getWidth(), topY, paint);

        paint.setColor(Color.parseColor("#FFD54F"));
        paint.setStrokeWidth(6f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, topY, getWidth(), topY, paint);
        paint.setStyle(Paint.Style.FILL);

        // 2. Update and Draw Game Engine
        if (gameEngine != null) {
            if (!isViewPaused) {
                gameEngine.update(dt);
            }
            gameEngine.draw(canvas, paint);
        }

        // 3. Continuously request redraw for smooth animations and physics when active
        if (!isViewPaused) {
            postInvalidateOnAnimation();
        }
    }
}
