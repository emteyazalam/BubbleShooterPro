package com.redcodersgroup.bubbleshooter.bubble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;
import com.redcodersgroup.bubbleshooter.R;
import com.redcodersgroup.bubbleshooter.board.GridPosition;

public class Bubble {
    private static Drawable bombDrawable;
    private static Bitmap bombBitmapCache;
    private static int cachedBitmapRadius = 0;

    private static Drawable rainbowDrawable;
    private static Bitmap rainbowBitmapCache;
    private static int cachedRainbowBitmapRadius = 0;

    private static Drawable lightningDrawable;
    private static Bitmap lightningBitmapCache;
    private static int cachedLightningBitmapRadius = 0;

    private static Drawable fireballDrawable;
    private static Bitmap fireballBitmapCache;
    private static int cachedFireballBitmapRadius = 0;

    private static Drawable stoneDrawable;
    private static Bitmap stoneBitmapCache;
    private static int cachedStoneBitmapRadius = 0;

    public static void initResources(Context context) {
        if (context != null) {
            try {
                bombDrawable = AppCompatResources.getDrawable(context.getApplicationContext(), R.drawable.bomb_bubble);
            } catch (Exception ignored) {}
            try {
                rainbowDrawable = AppCompatResources.getDrawable(context.getApplicationContext(), R.drawable.rainbow_bubble);
            } catch (Exception ignored) {}
            try {
                lightningDrawable = AppCompatResources.getDrawable(context.getApplicationContext(), R.drawable.lightning_bubble);
            } catch (Exception ignored) {}
            try {
                fireballDrawable = AppCompatResources.getDrawable(context.getApplicationContext(), R.drawable.fireball_bubble);
            } catch (Exception ignored) {}
            try {
                stoneDrawable = AppCompatResources.getDrawable(context.getApplicationContext(), R.drawable.stone_bubble);
            } catch (Exception ignored) {}
        }
    }

    private BubbleColor color;
    private BubbleType type;
    private GridPosition gridPosition;

    private float x;
    private float y;
    private float radius;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float alpha = 1.0f;

    // Animation state
    private boolean isPopping = false;
    private float popProgress = 0f;
    private boolean isFalling = false;
    private float vx = 0f;
    private float vy = 0f;

    public Bubble(BubbleColor color, BubbleType type, GridPosition position) {
        this.color = color;
        this.type = type;
        this.gridPosition = position;
    }

    public Bubble(BubbleColor color, GridPosition position) {
        this(color, BubbleType.NORMAL, position);
    }

    public BubbleColor getColor() {
        return color;
    }

    public void setColor(BubbleColor color) {
        this.color = color;
    }

    public BubbleType getType() {
        return type;
    }

    public void setType(BubbleType type) {
        this.type = type;
    }

    public GridPosition getGridPosition() {
        return gridPosition;
    }

    public void setGridPosition(GridPosition gridPosition) {
        this.gridPosition = gridPosition;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
    }

    public boolean isPopping() {
        return isPopping;
    }

    public void startPop() {
        this.isPopping = true;
        this.popProgress = 0f;
    }

    public float getPopProgress() {
        return popProgress;
    }

    public void setPopProgress(float popProgress) {
        this.popProgress = popProgress;
    }

    public boolean isFalling() {
        return isFalling;
    }

    public void startFalling(float initialVx, float initialVy) {
        this.isFalling = true;
        this.vx = initialVx;
        this.vy = initialVy;
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

    public void update(float dt) {
        if (isFalling) {
            // Apply gravity
            vy += 1800f * dt;
            x += vx * dt;
            y += vy * dt;
        }

        if (isPopping) {
            popProgress += dt * 4.0f; // 250ms pop
            scaleX = 1.0f + popProgress * 0.4f;
            scaleY = scaleX;
            alpha = Math.max(0f, 1.0f - popProgress);
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        if (alpha <= 0f || radius <= 0f) return;

        canvas.save();
        canvas.translate(x, y);
        canvas.scale(scaleX, scaleY);

        int originalAlpha = paint.getAlpha();
        paint.setAlpha((int) (255 * alpha));
        paint.setStyle(Paint.Style.FILL);

        if (type == BubbleType.BOMB) {
            drawBomb(canvas, paint);
        } else if (type == BubbleType.RAINBOW) {
            drawRainbow(canvas, paint);
        } else if (type == BubbleType.LIGHTNING) {
            drawLightning(canvas, paint);
        } else if (type == BubbleType.FIREBALL) {
            drawFireball(canvas, paint);
        } else if (type == BubbleType.STONE) {
            drawStone(canvas, paint);
        } else {
            drawGlossyBubble(canvas, paint, color);
        }

        paint.setShader(null);
        paint.setAlpha(originalAlpha);
        canvas.restore();
    }

    private void drawGlossyBubble(Canvas canvas, Paint paint, BubbleColor bColor) {
        // 1. Subtle drop shadow
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb((int) (60 * alpha), 0, 0, 0));
        canvas.drawCircle(0, radius * 0.12f, radius, paint);

        // 2. 100% Solid vibrant base color
        paint.setColor(bColor.primaryColor);
        paint.setAlpha((int) (255 * alpha));
        canvas.drawCircle(0, 0, radius, paint);

        // 3. 3D Spherical bevel shading (shaded edge at bottom/right)
        float lightOffset = radius * 0.25f;
        RadialGradient sphereGradient = new RadialGradient(
                -lightOffset, -lightOffset, radius * 1.3f,
                new int[]{Color.TRANSPARENT, Color.TRANSPARENT, bColor.darkColor},
                new float[]{0.0f, 0.65f, 1.0f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(sphereGradient);
        canvas.drawCircle(0, 0, radius, paint);
        paint.setShader(null);

        // 4. Crisp solid outer rim border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(radius * 0.08f);
        paint.setColor(bColor.darkColor);
        paint.setAlpha((int) (180 * alpha));
        canvas.drawCircle(0, 0, radius * 0.96f, paint);
        paint.setStyle(Paint.Style.FILL);

        // 5. Clean, compact top-left white shine (specular highlight)
        paint.setColor(Color.WHITE);
        paint.setAlpha((int) (200 * alpha));
        canvas.save();
        canvas.translate(-radius * 0.35f, -radius * 0.35f);
        canvas.rotate(-40f);
        canvas.drawOval(-radius * 0.26f, -radius * 0.13f, radius * 0.26f, radius * 0.13f, paint);
        canvas.restore();

        // 6. Tiny bright pinpoint sparkle
        paint.setAlpha((int) (240 * alpha));
        canvas.drawCircle(-radius * 0.22f, -radius * 0.52f, radius * 0.07f, paint);
    }

    private void drawBomb(Canvas canvas, Paint paint) {
        if (bombDrawable != null) {
            int size = (int) (radius * 2);
            if (size > 0) {
                if (bombBitmapCache == null || cachedBitmapRadius != size) {
                    cachedBitmapRadius = size;
                    try {
                        bombBitmapCache = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                        Canvas bmpCanvas = new Canvas(bombBitmapCache);
                        bombDrawable.setBounds(0, 0, size, size);
                        bombDrawable.draw(bmpCanvas);
                    } catch (Exception e) {
                        bombBitmapCache = null;
                    }
                }

                if (bombBitmapCache != null && !bombBitmapCache.isRecycled()) {
                    paint.setAlpha((int) (255 * alpha));
                    canvas.drawBitmap(bombBitmapCache, -radius, -radius, paint);
                    return;
                }
            }

            int r = (int) radius;
            bombDrawable.setBounds(-r, -r, r, r);
            bombDrawable.setAlpha((int) (255 * alpha));
            bombDrawable.draw(canvas);
            return;
        }

        // Fallback Metallic dark sphere
        RadialGradient bombGradient = new RadialGradient(
                -radius * 0.3f, -radius * 0.3f, radius * 1.3f,
                new int[]{Color.parseColor("#7F8C8D"), Color.parseColor("#2C3E50"), Color.parseColor("#1A1A1A")},
                new float[]{0.0f, 0.6f, 1.0f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(bombGradient);
        canvas.drawCircle(0, 0, radius, paint);
        paint.setShader(null);

        // Skull / Bomb emblem / Fuse
        paint.setColor(Color.parseColor("#E74C3C"));
        paint.setAlpha((int) (255 * alpha));
        canvas.drawCircle(0, 0, radius * 0.35f, paint);

        // White exclamation mark or fuse
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(radius * 0.12f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, -radius * 0.2f, 0, radius * 0.05f, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, radius * 0.18f, radius * 0.07f, paint);

        // Highlight
        paint.setColor(Color.WHITE);
        paint.setAlpha((int) (160 * alpha));
        canvas.drawCircle(-radius * 0.35f, -radius * 0.35f, radius * 0.15f, paint);
    }

    private void drawRainbow(Canvas canvas, Paint paint) {
        if (rainbowDrawable != null) {
            int size = (int) (radius * 2);
            if (size > 0) {
                if (rainbowBitmapCache == null || cachedRainbowBitmapRadius != size) {
                    cachedRainbowBitmapRadius = size;
                    try {
                        rainbowBitmapCache = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                        Canvas bmpCanvas = new Canvas(rainbowBitmapCache);
                        rainbowDrawable.setBounds(0, 0, size, size);
                        rainbowDrawable.draw(bmpCanvas);
                    } catch (Exception e) {
                        rainbowBitmapCache = null;
                    }
                }

                if (rainbowBitmapCache != null && !rainbowBitmapCache.isRecycled()) {
                    paint.setAlpha((int) (255 * alpha));
                    canvas.drawBitmap(rainbowBitmapCache, -radius, -radius, paint);
                    return;
                }
            }

            int r = (int) radius;
            rainbowDrawable.setBounds(-r, -r, r, r);
            rainbowDrawable.setAlpha((int) (255 * alpha));
            rainbowDrawable.draw(canvas);
            return;
        }

        int[] rainbowColors = new int[]{
                Color.parseColor("#FF3366"),
                Color.parseColor("#F1C40F"),
                Color.parseColor("#2ECC71"),
                Color.parseColor("#3498DB"),
                Color.parseColor("#9B59B6")
        };

        for (int i = 0; i < rainbowColors.length; i++) {
            paint.setColor(rainbowColors[i]);
            float r = radius * (1.0f - i * 0.18f);
            if (r > 0) {
                canvas.drawCircle(0, 0, r, paint);
            }
        }

        // Center white star / jewel
        paint.setColor(Color.WHITE);
        canvas.drawCircle(0, 0, radius * 0.22f, paint);

        // Gloss
        paint.setAlpha((int) (180 * alpha));
        canvas.drawCircle(-radius * 0.3f, -radius * 0.3f, radius * 0.2f, paint);
    }

    private void drawLightning(Canvas canvas, Paint paint) {
        if (lightningDrawable != null) {
            int size = (int) (radius * 2);
            if (size > 0) {
                if (lightningBitmapCache == null || cachedLightningBitmapRadius != size) {
                    cachedLightningBitmapRadius = size;
                    try {
                        lightningBitmapCache = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                        Canvas bmpCanvas = new Canvas(lightningBitmapCache);
                        lightningDrawable.setBounds(0, 0, size, size);
                        lightningDrawable.draw(bmpCanvas);
                    } catch (Exception e) {
                        lightningBitmapCache = null;
                    }
                }

                if (lightningBitmapCache != null && !lightningBitmapCache.isRecycled()) {
                    paint.setAlpha((int) (255 * alpha));
                    canvas.drawBitmap(lightningBitmapCache, -radius, -radius, paint);
                    return;
                }
            }

            int r = (int) radius;
            lightningDrawable.setBounds(-r, -r, r, r);
            lightningDrawable.setAlpha((int) (255 * alpha));
            lightningDrawable.draw(canvas);
            return;
        }

        // Fallback electric blue base with yellow core
        RadialGradient lightningGrad = new RadialGradient(
                -radius * 0.3f, -radius * 0.3f, radius * 1.3f,
                new int[]{Color.parseColor("#7D89FF"), Color.parseColor("#3A3FE0"), Color.parseColor("#14126B")},
                new float[]{0.0f, 0.5f, 1.0f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(lightningGrad);
        canvas.drawCircle(0, 0, radius, paint);
        paint.setShader(null);

        // Center electric bolt glow
        paint.setColor(Color.parseColor("#FFF59A"));
        paint.setAlpha((int) (240 * alpha));
        canvas.drawCircle(0, 0, radius * 0.45f, paint);

        // Highlight
        paint.setColor(Color.WHITE);
        paint.setAlpha((int) (200 * alpha));
        canvas.drawCircle(-radius * 0.3f, -radius * 0.3f, radius * 0.15f, paint);
    }

    private void drawFireball(Canvas canvas, Paint paint) {
        if (fireballDrawable != null) {
            int size = (int) (radius * 2);
            if (size > 0) {
                if (fireballBitmapCache == null || cachedFireballBitmapRadius != size) {
                    cachedFireballBitmapRadius = size;
                    try {
                        fireballBitmapCache = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                        Canvas bmpCanvas = new Canvas(fireballBitmapCache);
                        fireballDrawable.setBounds(0, 0, size, size);
                        fireballDrawable.draw(bmpCanvas);
                    } catch (Exception e) {
                        fireballBitmapCache = null;
                    }
                }

                if (fireballBitmapCache != null && !fireballBitmapCache.isRecycled()) {
                    paint.setAlpha((int) (255 * alpha));
                    canvas.drawBitmap(fireballBitmapCache, -radius, -radius, paint);
                    return;
                }
            }

            int r = (int) radius;
            fireballDrawable.setBounds(-r, -r, r, r);
            fireballDrawable.setAlpha((int) (255 * alpha));
            fireballDrawable.draw(canvas);
            return;
        }

        // Fallback molten flame core
        RadialGradient fireGrad = new RadialGradient(
                0, 0, radius,
                new int[]{Color.parseColor("#FFF5A0"), Color.parseColor("#FF6A0A"), Color.parseColor("#D42A00")},
                new float[]{0.0f, 0.5f, 1.0f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(fireGrad);
        canvas.drawCircle(0, 0, radius, paint);
        paint.setShader(null);
    }

    private void drawStone(Canvas canvas, Paint paint) {
        if (stoneDrawable != null) {
            int size = (int) (radius * 2);
            if (size > 0) {
                if (stoneBitmapCache == null || cachedStoneBitmapRadius != size) {
                    cachedStoneBitmapRadius = size;
                    try {
                        stoneBitmapCache = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                        Canvas bmpCanvas = new Canvas(stoneBitmapCache);
                        stoneDrawable.setBounds(0, 0, size, size);
                        stoneDrawable.draw(bmpCanvas);
                    } catch (Exception e) {
                        stoneBitmapCache = null;
                    }
                }

                if (stoneBitmapCache != null && !stoneBitmapCache.isRecycled()) {
                    paint.setAlpha((int) (255 * alpha));
                    canvas.drawBitmap(stoneBitmapCache, -radius, -radius, paint);
                    return;
                }
            }

            int r = (int) radius;
            stoneDrawable.setBounds(-r, -r, r, r);
            stoneDrawable.setAlpha((int) (255 * alpha));
            stoneDrawable.draw(canvas);
            return;
        }

        // Fallback stone rock texture
        RadialGradient stoneGrad = new RadialGradient(
                -radius * 0.3f, -radius * 0.3f, radius * 1.3f,
                new int[]{Color.parseColor("#C6CBD2"), Color.parseColor("#8B919B"), Color.parseColor("#23262C")},
                new float[]{0.0f, 0.5f, 1.0f},
                Shader.TileMode.CLAMP
        );
        paint.setShader(stoneGrad);
        canvas.drawCircle(0, 0, radius, paint);
        paint.setShader(null);
    }
}
