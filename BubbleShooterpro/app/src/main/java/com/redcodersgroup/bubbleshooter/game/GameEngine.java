package com.redcodersgroup.bubbleshooter.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.board.BubbleBoard;
import com.redcodersgroup.bubbleshooter.board.BubbleGrid;
import com.redcodersgroup.bubbleshooter.board.GridPosition;
import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import com.redcodersgroup.bubbleshooter.bubble.BubbleColor;
import com.redcodersgroup.bubbleshooter.bubble.BubbleProjectile;
import com.redcodersgroup.bubbleshooter.bubble.BubbleType;
import com.redcodersgroup.bubbleshooter.level.Level;
import com.redcodersgroup.bubbleshooter.level.LevelObjective;
import com.redcodersgroup.bubbleshooter.physics.CollisionDetector;
import com.redcodersgroup.bubbleshooter.physics.TrajectoryCalculator;
import com.redcodersgroup.bubbleshooter.physics.WallBounceCalculator;
import com.redcodersgroup.bubbleshooter.scoring.ComboManager;
import com.redcodersgroup.bubbleshooter.scoring.ScoreManager;
import com.redcodersgroup.bubbleshooter.visual.ConfettiSystem;
import com.redcodersgroup.bubbleshooter.visual.FloatingText;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameEngine {

    public interface GameEventListener {
        void onScoreUpdated(int score, int stars, float starProgress);
        void onShotsUpdated(int shotsRemaining);
        void onGameWon(int score, int stars);
        void onGameLost(int score);
    }

    private final Context context;
    private final SoundManager soundManager;
    private final BubbleGrid grid;
    private final BubbleBoard board;
    private final ScoreManager scoreManager;
    private final ComboManager comboManager;
    private final ConfettiSystem confettiSystem;
    private final Random random = new Random();

    private GameState state = GameState.READY;
    private Level currentLevel;
    private GameEventListener listener;

    private float boardLeft;
    private float boardRight;
    private float boardTop;
    private float boardBottom;
    private float bubbleRadius = 45f;
    private float launcherX;
    private float launcherY;
    private float previewX;
    private float previewY;

    private float aimAngleRad = (float) (-Math.PI / 2.0); // straight up
    private List<PointF> trajectoryPoints = new ArrayList<>();

    private Bubble currentBubble;
    private Bubble nextBubble;
    private BubbleProjectile activeProjectile;
    private int shotsRemaining = 25;

    private final List<Bubble> poppingBubbles = new ArrayList<>();
    private final List<Bubble> fallingBubbles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private float resolveTimer = 0f;
    private boolean isSuperAimActive = false;
    private boolean isAimCancelled = false;

    // Launcher reload jump & pop-in animation
    private boolean isLauncherReloading = false;
    private float reloadTimer = 0f;
    private static final float RELOAD_DURATION = 0.22f;

    // Launcher swap jump animation
    private boolean isSwapping = false;
    private float swapTimer = 0f;
    private static final float SWAP_DURATION = 0.20f;

    // Booster mod equip pop & jump animation
    private boolean isBoosterEquipping = false;
    private float boosterEquipTimer = 0f;
    private static final float BOOSTER_EQUIP_DURATION = 0.22f;

    public GameEngine(Context context) {
        this.context = context.getApplicationContext();
        this.soundManager = SoundManager.getInstance(context);
        this.grid = new BubbleGrid();
        this.board = new BubbleBoard(grid);
        this.scoreManager = new ScoreManager();
        this.comboManager = new ComboManager();
        this.confettiSystem = new ConfettiSystem();
        Bubble.initResources(this.context);
    }

    public void setEventListener(GameEventListener listener) {
        this.listener = listener;
    }

    public void setViewBounds(int width, int height) {
        float density = context.getResources().getDisplayMetrics().density;
        float topMargin = 68f * density; // Clear top HUD overlay cleanly
        this.boardLeft = 0f;
        this.boardRight = width;
        this.boardTop = topMargin;
        this.boardBottom = height;

        // 8 bubbles across on even row: width = 8 * 2 * radius = 16 * radius
        this.bubbleRadius = width / (BubbleGrid.COLS_EVEN * 2.0f);
        this.grid.setDimensions(boardLeft, boardTop, bubbleRadius);

        this.launcherX = width * 0.5f;
        this.launcherY = height - (130f * density); // Elevated ~50-60dp above bottom booster bar
        this.previewX = launcherX - (bubbleRadius * 2.85f);
        this.previewY = launcherY + (bubbleRadius * 0.15f);

        if (currentBubble != null) {
            currentBubble.setX(launcherX);
            currentBubble.setY(launcherY);
            currentBubble.setRadius(bubbleRadius);
            currentBubble.setScale(1.0f);
            currentBubble.setAlpha(1.0f);
        }
        if (nextBubble != null) {
            nextBubble.setX(previewX);
            nextBubble.setY(previewY);
            nextBubble.setRadius(bubbleRadius * 0.75f);
            nextBubble.setScale(1.0f);
            nextBubble.setAlpha(1.0f);
        }

        updateTrajectory();
    }

    public float getBoardTop() {
        return boardTop;
    }

    public void loadLevel(Level level) {
        this.currentLevel = level;
        this.shotsRemaining = level.getMaxShots();
        this.scoreManager.reset();
        this.scoreManager.setStarThresholds(level.getStarThresholds());
        this.comboManager.reset();
        this.poppingBubbles.clear();
        this.fallingBubbles.clear();
        this.floatingTexts.clear();
        this.confettiSystem.clear();
        this.grid.clear();
        this.state = GameState.READY;
        this.isLauncherReloading = false;

        // Populate grid from level row strings
        List<String> rows = level.getRows();
        for (int r = 0; r < rows.size() && r < BubbleGrid.MAX_ROWS; r++) {
            String rowStr = rows.get(r);
            int maxCols = (r % 2 == 0) ? BubbleGrid.COLS_EVEN : BubbleGrid.COLS_ODD;
            for (int c = 0; c < rowStr.length() && c < maxCols; c++) {
                char ch = rowStr.charAt(c);
                if (ch == '.' || ch == ' ') continue;

                BubbleType bType = BubbleType.NORMAL;
                BubbleColor bColor;
                if (ch == 'X' || ch == 'x') {
                    bType = BubbleType.BOMB;
                    bColor = BubbleColor.BOMB;
                } else if (ch == '*') {
                    bType = BubbleType.RAINBOW;
                    bColor = BubbleColor.RAINBOW;
                } else if (ch == 'L' || ch == 'l') {
                    bType = BubbleType.LIGHTNING;
                    bColor = BubbleColor.LIGHTNING;
                } else if (ch == 'F' || ch == 'f') {
                    bType = BubbleType.FIREBALL;
                    bColor = BubbleColor.FIREBALL;
                } else if (ch == 'S' || ch == 's') {
                    bType = BubbleType.STONE;
                    bColor = BubbleColor.STONE;
                } else {
                    bColor = BubbleColor.fromChar(ch);
                }

                if (bColor != BubbleColor.NONE) {
                    Bubble b = new Bubble(bColor, bType, new GridPosition(r, c));
                    grid.setBubble(r, c, b);
                }
            }
        }

        // Initialize launcher bubbles
        this.currentBubble = new Bubble(pickRandomColor(), BubbleType.NORMAL, null);
        this.currentBubble.setX(launcherX);
        this.currentBubble.setY(launcherY);
        this.currentBubble.setRadius(bubbleRadius);
        this.currentBubble.setScale(1.0f);
        this.currentBubble.setAlpha(1.0f);

        this.nextBubble = new Bubble(pickRandomColor(), BubbleType.NORMAL, null);
        this.nextBubble.setX(previewX);
        this.nextBubble.setY(previewY);
        this.nextBubble.setRadius(bubbleRadius * 0.75f);
        this.nextBubble.setScale(1.0f);
        this.nextBubble.setAlpha(1.0f);

        if (listener != null) {
            listener.onScoreUpdated(scoreManager.getScore(), scoreManager.getStarsEarned(), scoreManager.getStarProgress());
            listener.onShotsUpdated(shotsRemaining);
        }

        updateTrajectory();
    }

    private BubbleColor pickRandomColor() {
        // Collect colors still present on the board
        List<BubbleColor> existingOnBoard = new ArrayList<>();
        for (Bubble b : grid.getAllBubbles()) {
            if (b != null && b.getType() == BubbleType.NORMAL) {
                if (!existingOnBoard.contains(b.getColor())) {
                    existingOnBoard.add(b.getColor());
                }
            }
        }

        if (!existingOnBoard.isEmpty()) {
            return existingOnBoard.get(random.nextInt(existingOnBoard.size()));
        }

        // Fallback to level available colors
        if (currentLevel != null && !currentLevel.getAvailableColors().isEmpty()) {
            List<BubbleColor> avail = currentLevel.getAvailableColors();
            return avail.get(random.nextInt(avail.size()));
        }

        return BubbleColor.RED;
    }

    public void swapBubbles() {
        if (state != GameState.READY && state != GameState.AIMING) return;
        if (currentBubble == null || nextBubble == null) return;

        // Cancel other animations
        isLauncherReloading = false;
        isBoosterEquipping = false;

        // Swap logical colors and types
        BubbleColor tempColor = currentBubble.getColor();
        BubbleType tempType = currentBubble.getType();

        currentBubble.setColor(nextBubble.getColor());
        currentBubble.setType(nextBubble.getType());

        nextBubble.setColor(tempColor);
        nextBubble.setType(tempType);

        // Start swap jump animation:
        isSwapping = true;
        swapTimer = 0f;

        soundManager.playClick();
        updateTrajectory();
    }

    public void equipBooster(BubbleType type) {
        if (state != GameState.READY && state != GameState.AIMING) return;
        if (currentBubble == null) return;

        // Cancel other animations
        isLauncherReloading = false;
        isSwapping = false;

        currentBubble.setType(type);
        if (type == BubbleType.BOMB) {
            currentBubble.setColor(BubbleColor.BOMB);
        } else if (type == BubbleType.RAINBOW) {
            currentBubble.setColor(BubbleColor.RAINBOW);
        } else if (type == BubbleType.LIGHTNING) {
            currentBubble.setColor(BubbleColor.LIGHTNING);
        } else if (type == BubbleType.FIREBALL) {
            currentBubble.setColor(BubbleColor.FIREBALL);
        }

        // Start booster mod equip jump & pop animation
        isBoosterEquipping = true;
        boosterEquipTimer = 0f;

        // Sparkle burst particles around launcher base
        int particleColor = currentBubble.getColor().primaryColor;
        confettiSystem.spawnPopParticles(launcherX, launcherY, particleColor, 12);

        soundManager.playClick();
        updateTrajectory();
    }

    public void onTouchDown(float touchX, float touchY) {
        if (state != GameState.READY && state != GameState.AIMING) return;

        // Check if user tapped preview bubble, launcher base, or the swap icon area to swap
        float midX = (launcherX + previewX) / 2f;
        float midY = (launcherY + previewY) / 2f;
        float distToMid = (float) Math.hypot(touchX - midX, touchY - midY);
        float distToPreview = (float) Math.hypot(touchX - previewX, touchY - previewY);
        if (distToPreview <= bubbleRadius * 1.6f || distToMid <= bubbleRadius * 1.5f) {
            swapBubbles();
            return;
        }

        // Check if user tapped launcher
        float distToLauncher = (float) Math.hypot(touchX - launcherX, touchY - launcherY);
        if (distToLauncher <= bubbleRadius * 1.2f) {
            swapBubbles();
            return;
        }

        // Imaginary cancel threshold line: slightly above the launcher
        float cancelThreshold = launcherY - (bubbleRadius * 0.4f);
        if (touchY < cancelThreshold) {
            state = GameState.AIMING;
            isAimCancelled = false;
            updateAimAngle(touchX, touchY);
        }
    }

    public void onTouchMove(float touchX, float touchY) {
        if (state == GameState.AIMING) {
            float cancelThreshold = launcherY - (bubbleRadius * 0.4f);
            if (touchY >= cancelThreshold) {
                // Aim dragged below imaginary line -> cancel shot and hide laser
                isAimCancelled = true;
                if (trajectoryPoints != null) {
                    trajectoryPoints.clear();
                }
            } else {
                isAimCancelled = false;
                updateAimAngle(touchX, touchY);
            }
        }
    }

    public void onTouchUp(float touchX, float touchY) {
        if (state == GameState.AIMING) {
            float cancelThreshold = launcherY - (bubbleRadius * 0.4f);
            if (isAimCancelled || touchY >= cancelThreshold) {
                // Cancelled shot: reset to READY, clear trajectory, do NOT launch projectile
                state = GameState.READY;
                isAimCancelled = false;
                if (trajectoryPoints != null) {
                    trajectoryPoints.clear();
                }
            } else {
                shoot();
            }
        }
    }

    private void updateAimAngle(float touchX, float touchY) {
        float dx = touchX - launcherX;
        float dy = touchY - launcherY;
        float angle = (float) Math.atan2(dy, dx);

        // Clamp aiming angle: must shoot upward
        // Range: -168 degrees (-2.93 rad) to -12 degrees (-0.21 rad)
        float minAngle = (float) (-Math.PI + 0.20);
        float maxAngle = -0.20f;

        if (angle < minAngle) angle = minAngle;
        if (angle > maxAngle && angle < 0) angle = maxAngle;
        if (angle >= 0) {
            angle = (dx < 0) ? minAngle : maxAngle;
        }

        this.aimAngleRad = angle;
        updateTrajectory();
    }

    private void updateTrajectory() {
        this.trajectoryPoints = TrajectoryCalculator.calculateTrajectory(
                launcherX, launcherY, aimAngleRad,
                boardLeft, boardRight, boardTop,
                grid, bubbleRadius
        );
    }

    private void shoot() {
        if (currentBubble == null || shotsRemaining <= 0) return;

        // Finish any active swap or booster animation immediately
        if (isSwapping) {
            isSwapping = false;
            currentBubble.setX(launcherX);
            currentBubble.setY(launcherY);
            currentBubble.setScale(1.0f);
            nextBubble.setX(previewX);
            nextBubble.setY(previewY);
            nextBubble.setScale(1.0f);
        }
        if (isBoosterEquipping) {
            isBoosterEquipping = false;
            currentBubble.setX(launcherX);
            currentBubble.setY(launcherY);
            currentBubble.setScale(1.0f);
        }

        state = GameState.SHOOTING;
        activeProjectile = new BubbleProjectile(currentBubble.getColor(), currentBubble.getType(), bubbleRadius);
        float dirX = (float) Math.cos(aimAngleRad);
        float dirY = (float) Math.sin(aimAngleRad);
        activeProjectile.launch(launcherX, launcherY, dirX, dirY);

        soundManager.playShoot();

        // 1. Promote queued bubble into currentBubble, starting at preview position
        currentBubble.setColor(nextBubble.getColor());
        currentBubble.setType(nextBubble.getType());
        currentBubble.setRadius(bubbleRadius);
        currentBubble.setX(previewX);
        currentBubble.setY(previewY);
        currentBubble.setScale(0.75f);
        currentBubble.setAlpha(1.0f);

        // 2. Pick a new random nextBubble and prepare it to pop into the preview position
        nextBubble.setColor(pickRandomColor());
        nextBubble.setType(BubbleType.NORMAL);
        nextBubble.setRadius(bubbleRadius * 0.75f);
        nextBubble.setX(previewX);
        nextBubble.setY(previewY);
        nextBubble.setScale(0.0f);
        nextBubble.setAlpha(0.0f);

        // 3. Trigger jump & pop reload animation
        isLauncherReloading = true;
        reloadTimer = 0f;
    }

    public void update(float dt) {
        if (state == GameState.PAUSED) {
            return;
        }

        // 0. Update launcher reload jump & pop-in animation
        if (isLauncherReloading) {
            reloadTimer += dt;
            float t = Math.min(1.0f, reloadTimer / RELOAD_DURATION);

            // Parabolic hop arc from preview to launcher
            float hopHeight = bubbleRadius * 0.70f;
            float hop = (float) Math.sin(t * Math.PI) * hopHeight;

            float curX = previewX + (launcherX - previewX) * t;
            float curY = previewY + (launcherY - previewY) * t - hop;
            float curScale = 0.75f + 0.25f * t;

            if (currentBubble != null) {
                currentBubble.setX(curX);
                currentBubble.setY(curY);
                currentBubble.setScale(curScale);
            }

            // Pop-in with elastic bounce for new queued bubble
            float popT = Math.max(0f, (reloadTimer - 0.03f) / (RELOAD_DURATION * 0.85f));
            popT = Math.min(1.0f, popT);
            float p = popT - 1.0f;
            float popScale = (popT == 0f) ? 0f : (p * p * (2.2f * p + 1.2f) + 1.0f);

            if (nextBubble != null) {
                nextBubble.setScale(Math.max(0f, popScale));
                nextBubble.setAlpha(Math.min(1.0f, popT * 2.5f));
            }

            if (t >= 1.0f) {
                isLauncherReloading = false;
                if (currentBubble != null) {
                    currentBubble.setX(launcherX);
                    currentBubble.setY(launcherY);
                    currentBubble.setScale(1.0f);
                    currentBubble.setAlpha(1.0f);
                }
                if (nextBubble != null) {
                    nextBubble.setX(previewX);
                    nextBubble.setY(previewY);
                    nextBubble.setScale(1.0f);
                    nextBubble.setAlpha(1.0f);
                }
            }
        }

        // 0.1 Update launcher swap jump animation
        if (isSwapping) {
            swapTimer += dt;
            float t = Math.min(1.0f, swapTimer / SWAP_DURATION);

            // currentBubble hops OVER top from preview to launcher
            float hopUp = (float) Math.sin(t * Math.PI) * (bubbleRadius * 0.65f);
            float curX = previewX + (launcherX - previewX) * t;
            float curY = previewY + (launcherY - previewY) * t - hopUp;
            float curScale = 0.75f + 0.25f * t;

            if (currentBubble != null) {
                currentBubble.setX(curX);
                currentBubble.setY(curY);
                currentBubble.setScale(curScale);
                currentBubble.setAlpha(1.0f);
            }

            // nextBubble dips UNDER from launcher to preview
            float dipDown = (float) Math.sin(t * Math.PI) * (bubbleRadius * 0.45f);
            float nxtX = launcherX + (previewX - launcherX) * t;
            float nxtY = launcherY + (previewY - launcherY) * t + dipDown;
            float nxtScale = 1.333f - 0.333f * t;

            if (nextBubble != null) {
                nextBubble.setX(nxtX);
                nextBubble.setY(nxtY);
                nextBubble.setScale(nxtScale);
                nextBubble.setAlpha(1.0f);
            }

            if (t >= 1.0f) {
                isSwapping = false;
                if (currentBubble != null) {
                    currentBubble.setX(launcherX);
                    currentBubble.setY(launcherY);
                    currentBubble.setScale(1.0f);
                    currentBubble.setAlpha(1.0f);
                }
                if (nextBubble != null) {
                    nextBubble.setX(previewX);
                    nextBubble.setY(previewY);
                    nextBubble.setScale(1.0f);
                    nextBubble.setAlpha(1.0f);
                }
            }
        }

        // 0.2 Update booster mod equip pop & jump animation
        if (isBoosterEquipping) {
            boosterEquipTimer += dt;
            float t = Math.min(1.0f, boosterEquipTimer / BOOSTER_EQUIP_DURATION);

            // Elastic pop scale: 0.3 -> 1.25 -> 1.0
            float hop = (float) Math.sin(t * Math.PI) * (bubbleRadius * 0.45f);
            float curY = launcherY - hop;
            float p = t - 1.0f;
            float scale = (p * p * (2.4f * p + 1.4f) + 1.0f);
            scale = Math.max(0.3f, scale);

            if (currentBubble != null) {
                currentBubble.setX(launcherX);
                currentBubble.setY(curY);
                currentBubble.setScale(scale);
                currentBubble.setAlpha(Math.min(1.0f, t * 3.0f));
            }

            if (t >= 1.0f) {
                isBoosterEquipping = false;
                if (currentBubble != null) {
                    currentBubble.setX(launcherX);
                    currentBubble.setY(launcherY);
                    currentBubble.setScale(1.0f);
                    currentBubble.setAlpha(1.0f);
                }
            }
        }

        // 1. Update visual particles and texts
        confettiSystem.update(dt);

        Iterator<FloatingText> textIt = floatingTexts.iterator();
        while (textIt.hasNext()) {
            FloatingText ft = textIt.next();
            ft.update(dt);
            if (!ft.isAlive()) textIt.remove();
        }

        Iterator<Bubble> popIt = poppingBubbles.iterator();
        while (popIt.hasNext()) {
            Bubble b = popIt.next();
            b.update(dt);
            if (b.getPopProgress() >= 1.0f) popIt.remove();
        }

        Iterator<Bubble> fallIt = fallingBubbles.iterator();
        while (fallIt.hasNext()) {
            Bubble b = fallIt.next();
            b.update(dt);
            if (b.getY() > boardBottom + bubbleRadius * 2) {
                fallIt.remove();
            }
        }

        // 2. Projectile Movement and Collision
        if (state == GameState.SHOOTING && activeProjectile != null) {
            activeProjectile.update(dt);

            boolean bounced = WallBounceCalculator.checkAndHandleWallBounce(activeProjectile, boardLeft, boardRight);
            if (bounced) {
                soundManager.playBounce();
            }

            CollisionDetector.CollisionResult collision =
                    CollisionDetector.checkCollision(activeProjectile, board, boardTop);

            if (collision.collided) {
                resolveCollision(collision.snapPosition);
            }
        }

        // 3. Resolving Timer
        if (state == GameState.RESOLVING) {
            resolveTimer -= dt;
            if (resolveTimer <= 0f) {
                finishResolution();
            }
        }
    }

    private void resolveCollision(GridPosition snapPos) {
        state = GameState.RESOLVING;
        resolveTimer = 0.28f; // time for pop/fall animation to start

        if (snapPos == null) {
            // Fallback: nearest position in row 0
            snapPos = board.findNearestSnapPosition(activeProjectile.getX(), boardTop + bubbleRadius);
        }

        if (snapPos != null) {
            Bubble snapped = new Bubble(activeProjectile.getColor(), activeProjectile.getType(), snapPos);
            grid.setBubble(snapPos, snapped);

            // Evaluate matches
            List<GridPosition> matches = board.findMatches(snapPos);
            if (!matches.isEmpty()) {
                boolean hadBomb = (activeProjectile.getType() == BubbleType.BOMB);
                if (!hadBomb) {
                    for (GridPosition pos : matches) {
                        Bubble b = grid.getBubble(pos);
                        if (b != null && (b.getType() == BubbleType.BOMB || b.getColor() == BubbleColor.BOMB)) {
                            hadBomb = true;
                            break;
                        }
                    }
                }

                if (hadBomb) {
                    soundManager.playBomb();
                } else {
                    soundManager.playPop(comboManager.getStreak());
                }

                // Remove and animate matched bubbles
                for (GridPosition pos : matches) {
                    Bubble popped = grid.removeBubble(pos);
                    if (popped != null) {
                        popped.startPop();
                        poppingBubbles.add(popped);
                        int particleColor = (popped.getColor() == BubbleColor.BOMB || popped.getType() == BubbleType.BOMB)
                                ? Color.parseColor("#FF6D00")
                                : popped.getColor().primaryColor;
                        confettiSystem.spawnPopParticles(popped.getX(), popped.getY(), particleColor, hadBomb ? 20 : 12);
                    }
                }

                // Identify floating bubbles
                List<Bubble> floating = board.findFloatingBubbles();
                for (Bubble fb : floating) {
                    float vx = (float) ((Math.random() - 0.5) * 450.0);
                    float vy = (float) (-150 - Math.random() * 200.0);
                    fb.startFalling(vx, vy);
                    fallingBubbles.add(fb);
                }

                // Update combo and scoring
                comboManager.registerSuccess();
                int multiplier = comboManager.getMultiplier();
                int popScore = scoreManager.addPoppedBubbles(matches.size(), multiplier);
                int dropScore = scoreManager.addDroppedBubbles(floating.size(), multiplier);
                int totalTurnScore = popScore + dropScore;

                // Floating text feedback
                float textX = grid.getCenterX(snapPos.row, snapPos.col);
                float textY = grid.getCenterY(snapPos.row);
                if (hadBomb) {
                    floatingTexts.add(new FloatingText("BOOM! +" + totalTurnScore, textX, textY, Color.parseColor("#FF5722"), 48f, 1.3f));
                } else {
                    floatingTexts.add(new FloatingText("+" + totalTurnScore, textX, textY, Color.parseColor("#FFF176"), 42f, 1.1f));
                }

                String praise = comboManager.getPraiseText();
                if (!praise.isEmpty()) {
                    floatingTexts.add(new FloatingText(praise, textX, textY - 60f, Color.parseColor("#FF4081"), 48f, 1.4f));
                }

                // Update objective progress
                if (currentLevel != null) {
                    LevelObjective obj = currentLevel.getObjective();
                    if (obj.getType() == LevelObjective.Type.DROP_COUNT) {
                        obj.addProgress(floating.size());
                    } else if (obj.getType() == LevelObjective.Type.POP_COLOR) {
                        int matchedTargetColor = 0;
                        for (GridPosition pos : matches) {
                            if (activeProjectile.getColor() == obj.getTargetColor()) matchedTargetColor++;
                        }
                        obj.addProgress(matchedTargetColor);
                    }
                }

            } else {
                // Miss
                comboManager.registerMiss();
                soundManager.playBounce();
            }
        }

        shotsRemaining--;
        if (listener != null) {
            listener.onScoreUpdated(scoreManager.getScore(), scoreManager.getStarsEarned(), scoreManager.getStarProgress());
            listener.onShotsUpdated(shotsRemaining);
        }

        activeProjectile = null;
    }

    private void finishResolution() {
        // Check win condition
        boolean won = false;
        if (currentLevel != null && currentLevel.getObjective().isMet(board, scoreManager)) {
            won = true;
        } else if (grid.getBubbleCount() == 0) {
            won = true;
        }

        if (won) {
            state = GameState.WIN;
            soundManager.playWin();
            confettiSystem.spawnCelebrationBurst(boardRight, boardBottom, 70);
            if (listener != null) {
                listener.onGameWon(scoreManager.getScore(), Math.max(1, scoreManager.getStarsEarned()));
            }
            return;
        }

        // Check lose condition
        boolean lost = false;
        if (shotsRemaining <= 0) {
            lost = true;
        } else if (grid.getLowestOccupiedRow() >= 18) { // bottom limit
            lost = true;
        }

        if (lost) {
            state = GameState.LOSE;
            if (listener != null) {
                listener.onGameLost(scoreManager.getScore());
            }
            return;
        }

        state = GameState.READY;
        updateTrajectory();
    }

    public void draw(Canvas canvas, Paint paint) {
        // 1. Draw Trajectory Dots (ONLY when actively AIMING and not cancelled)
        if (state == GameState.AIMING && !isAimCancelled && trajectoryPoints != null && !trajectoryPoints.isEmpty()) {
            paint.setStyle(Paint.Style.FILL);
            int dotColor = (currentBubble != null) ? currentBubble.getColor().lightColor : Color.WHITE;
            paint.setColor(dotColor);

            int total = trajectoryPoints.size();
            for (int i = 0; i < total; i++) {
                PointF pt = trajectoryPoints.get(i);
                float radiusFactor = 0.22f + 0.15f * ((float) (total - i) / total);
                float dotR = bubbleRadius * radiusFactor;

                paint.setAlpha(180);
                canvas.drawCircle(pt.x, pt.y, dotR, paint);

                paint.setColor(Color.WHITE);
                paint.setAlpha(240);
                canvas.drawCircle(pt.x, pt.y, dotR * 0.45f, paint);
                paint.setColor(dotColor);
            }
        }

        // 2. Draw Board Bubbles
        for (Bubble b : grid.getAllBubbles()) {
            if (b != null) {
                b.draw(canvas, paint);
            }
        }

        // 3. Draw Popping and Falling Bubbles
        for (Bubble b : poppingBubbles) {
            b.draw(canvas, paint);
        }
        for (Bubble b : fallingBubbles) {
            b.draw(canvas, paint);
        }

        // 4. Draw Projectile
        if (activeProjectile != null) {
            activeProjectile.draw(canvas, paint);
        }

        // 5. Draw Launcher Base & Bubbles
        drawLauncher(canvas, paint);

        // 6. Draw Particles and Confetti
        confettiSystem.draw(canvas, paint);

        // 7. Draw Floating Texts
        for (FloatingText ft : floatingTexts) {
            ft.draw(canvas, paint);
        }
    }

    private void drawLauncher(Canvas canvas, Paint paint) {
        // Launcher Stand / Pedestal (cute wooden / metallic casual design)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#424242"));
        canvas.drawCircle(launcherX, launcherY, bubbleRadius * 1.35f, paint);

        paint.setColor(Color.parseColor("#FFD54F"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(bubbleRadius * 0.18f);
        canvas.drawCircle(launcherX, launcherY, bubbleRadius * 1.3f, paint);
        paint.setStyle(Paint.Style.FILL);

        // Preview Bubble Pedestal
        paint.setColor(Color.parseColor("#616161"));
        canvas.drawCircle(previewX, previewY, bubbleRadius * 0.95f, paint);

        // Ready Bubble in Launcher (drawn jumping or resting)
        if (currentBubble != null) {
            currentBubble.draw(canvas, paint);
        }

        // Preview Bubble (drawn popping in or resting)
        if (nextBubble != null) {
            nextBubble.draw(canvas, paint);
        }

        // Swap Icon Indicator (curved rotation arrows between preview and launcher)
        float midX = (launcherX + previewX) / 2f;
        float midY = (launcherY + previewY) / 2f;
        drawSwapIcon(canvas, paint, midX, midY, bubbleRadius * 0.95f);
    }

    private void drawSwapIcon(Canvas canvas, Paint paint, float cx, float cy, float size) {
        // Semi-transparent circular background chip
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(110, 0, 0, 0));
        canvas.drawCircle(cx, cy, size * 0.70f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.WHITE);
        float strokeW = size * 0.13f;
        paint.setStrokeWidth(strokeW);

        float r = size * 0.44f;
        RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);

        // Top arc (sweeps across top from left to right)
        canvas.drawArc(oval, 215, 110, false, paint);

        // Top arrowhead at the right end of the arc
        float topEndX = cx + r * (float) Math.cos(Math.toRadians(325));
        float topEndY = cy + r * (float) Math.sin(Math.toRadians(325));
        float headLen = size * 0.28f;

        Path topHead = new Path();
        topHead.moveTo(topEndX - headLen, topEndY);
        topHead.lineTo(topEndX, topEndY);
        topHead.lineTo(topEndX, topEndY + headLen);
        canvas.drawPath(topHead, paint);

        // Bottom arc (sweeps across bottom from right to left)
        canvas.drawArc(oval, 35, 110, false, paint);

        // Bottom arrowhead at the left end of the arc
        float botEndX = cx + r * (float) Math.cos(Math.toRadians(145));
        float botEndY = cy + r * (float) Math.sin(Math.toRadians(145));

        Path botHead = new Path();
        botHead.moveTo(botEndX + headLen, botEndY);
        botHead.lineTo(botEndX, botEndY);
        botHead.lineTo(botEndX, botEndY - headLen);
        canvas.drawPath(botHead, paint);

        paint.setStyle(Paint.Style.FILL);
    }

    public GameState getState() {
        return state;
    }

    private GameState stateBeforePause = GameState.READY;

    public void pause() {
        if (state != GameState.WIN && state != GameState.LOSE && state != GameState.PAUSED) {
            stateBeforePause = (state == GameState.AIMING) ? GameState.READY : state;
            state = GameState.PAUSED;
        }
    }

    public void resume() {
        if (state == GameState.PAUSED) {
            state = (stateBeforePause != null && stateBeforePause != GameState.PAUSED) ? stateBeforePause : GameState.READY;
        }
    }

    public int getShotsRemaining() {
        return shotsRemaining;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public BubbleBoard getBoard() {
        return board;
    }
}
