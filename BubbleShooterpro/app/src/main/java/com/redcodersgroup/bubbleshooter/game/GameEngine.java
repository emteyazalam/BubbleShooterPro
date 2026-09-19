package com.redcodersgroup.bubbleshooter.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import com.redcodersgroup.bubbleshooter.audio.SoundManager;
import com.redcodersgroup.bubbleshooter.board.BubbleBoard;
import com.redcodersgroup.bubbleshooter.board.BubbleGrid;
import com.redcodersgroup.bubbleshooter.board.GridPosition;
import com.redcodersgroup.bubbleshooter.board.NeighborCalculator;
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
        default void onGameLost(int score, String reason) {
            onGameLost(score);
        }
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
    private float deadlineY;
    private float dangerPulseTimer = 0f;
    private DashPathEffect normalDashEffect;
    private DashPathEffect dangerDashEffect;

    private float aimAngleRad = (float) (-Math.PI / 2.0); // straight up
    private List<PointF> trajectoryPoints = new ArrayList<>();
    private final Path laserPath = new Path();

    private Bubble currentBubble;
    private Bubble nextBubble;
    private BubbleProjectile activeProjectile;
    private int shotsRemaining = 25;
    private boolean isEndlessMode = false;
    private int endlessWaveCount = 1;
    private int endlessHighScore = 0;
    private List<BubbleColor> endlessColorsPool = new ArrayList<>();
    private final java.util.LinkedList<List<Bubble>> pregeneratedRowQueue = new java.util.LinkedList<>();

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

    private float customTopMargin = -1f;
    private int lastViewWidth = 0;
    private int lastViewHeight = 0;

    public void setEventListener(GameEventListener listener) {
        this.listener = listener;
    }

    public void setTopMargin(float topMarginPx) {
        this.customTopMargin = topMarginPx;
        if (lastViewWidth > 0 && lastViewHeight > 0) {
            setViewBounds(lastViewWidth, lastViewHeight);
        }
    }

    public void setViewBounds(int width, int height) {
        this.lastViewWidth = width;
        this.lastViewHeight = height;

        float density = context.getResources().getDisplayMetrics().density;
        float topMargin = (customTopMargin > 0) ? customTopMargin : (92f * density);

        // Tablet & wide screen optimization:
        // On phones (aspect ratio 9:16 to 9:21), board spans the full width.
        // On tablets / wide screens (4:3, 16:10 or landscape), constrain playfield board width
        // so bubbles maintain ideal crisp proportions and avoid taking over the entire screen.
        float maxBoardWidth = Math.min(width, Math.min(height * 0.62f, 560f * density));
        float boardWidth = Math.min(width, maxBoardWidth);

        this.boardLeft = (width - boardWidth) / 2.0f;
        this.boardRight = this.boardLeft + boardWidth;
        this.boardTop = topMargin;
        this.boardBottom = height;

        // 9 bubbles across on even row: boardWidth = 9 * 2 * radius = 18 * radius
        this.bubbleRadius = boardWidth / (BubbleGrid.COLS_EVEN * 2.0f);
        this.grid.setDimensions(boardLeft, boardTop, bubbleRadius);

        this.launcherX = boardLeft + (boardWidth * 0.5f);
        this.launcherY = height - (130f * density); // Elevated ~50-60dp above bottom booster bar
        this.previewX = launcherX - (bubbleRadius * 2.85f);
        this.previewY = launcherY + (bubbleRadius * 0.15f);
        this.deadlineY = launcherY - (bubbleRadius * 1.35f);
        this.normalDashEffect = new DashPathEffect(new float[]{16f, 12f}, 0);
        this.dangerDashEffect = new DashPathEffect(new float[]{18f, 8f}, 0);

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

    public float getBoardLeft() {
        return boardLeft;
    }

    public float getBoardRight() {
        return boardRight;
    }

    public float getBubbleRadius() {
        return bubbleRadius;
    }

    public void loadLevel(Level level) {
        this.isEndlessMode = false;
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

        // Initialize launcher bubbles with smart frontier and danger-aware colors
        BubbleColor firstColor = pickSmartLauncherColor(null);
        this.currentBubble = new Bubble(firstColor, BubbleType.NORMAL, null);
        this.currentBubble.setX(launcherX);
        this.currentBubble.setY(launcherY);
        this.currentBubble.setRadius(bubbleRadius);
        this.currentBubble.setScale(1.0f);
        this.currentBubble.setAlpha(1.0f);

        BubbleColor secondColor = pickSmartLauncherColor(firstColor);
        this.nextBubble = new Bubble(secondColor, BubbleType.NORMAL, null);
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

    public void loadEndlessMode(int personalBestHighScore, List<BubbleColor> colorsPool) {
        this.isEndlessMode = true;
        this.currentLevel = null;
        this.endlessWaveCount = 1;
        this.endlessHighScore = personalBestHighScore;
        this.shotsRemaining = 999999;
        this.endlessColorsPool = (colorsPool != null && !colorsPool.isEmpty())
                ? new ArrayList<>(colorsPool)
                : BubbleColor.getPlayableColors();

        this.scoreManager.reset();
        if (personalBestHighScore > 0) {
            int t1 = Math.max(500, (int) (personalBestHighScore * 0.35f));
            int t2 = Math.max(1000, (int) (personalBestHighScore * 0.70f));
            int t3 = Math.max(1500, personalBestHighScore);
            this.scoreManager.setStarThresholds(new int[]{t1, t2, t3});
        } else {
            this.scoreManager.setStarThresholds(new int[]{1000, 2500, 5000});
        }

        this.comboManager.reset();
        this.poppingBubbles.clear();
        this.fallingBubbles.clear();
        this.floatingTexts.clear();
        this.confettiSystem.clear();
        this.state = GameState.READY;
        this.isLauncherReloading = false;

        // Structured playable initial board using pattern generator
        List<BubbleColor> activeColors = EndlessPatternGenerator.getActiveColors(endlessWaveCount, endlessColorsPool);
        EndlessPatternGenerator.populateInitialBoard(this.grid, 5, activeColors, random);

        // Pre-generate the next 3 rows so they are immediately buffered for smooth streaming
        this.pregeneratedRowQueue.clear();
        int nextParity = grid.getRowParity() ^ 1;
        List<List<Bubble>> initialUpcoming = EndlessPatternGenerator.generateMultiRowChunk(
                endlessWaveCount + 1,
                nextParity,
                3,
                activeColors,
                random
        );
        this.pregeneratedRowQueue.addAll(initialUpcoming);

        // Initialize launcher bubbles with smart frontier and danger-aware colors
        BubbleColor firstColor = pickSmartLauncherColor(null);
        this.currentBubble = new Bubble(firstColor, BubbleType.NORMAL, null);
        this.currentBubble.setX(launcherX);
        this.currentBubble.setY(launcherY);
        this.currentBubble.setRadius(bubbleRadius);
        this.currentBubble.setScale(1.0f);
        this.currentBubble.setAlpha(1.0f);

        BubbleColor secondColor = pickSmartLauncherColor(firstColor);
        this.nextBubble = new Bubble(secondColor, BubbleType.NORMAL, null);
        this.nextBubble.setX(previewX);
        this.nextBubble.setY(previewY);
        this.nextBubble.setRadius(bubbleRadius * 0.75f);
        this.nextBubble.setScale(1.0f);
        this.nextBubble.setAlpha(1.0f);

        if (listener != null) {
            listener.onScoreUpdated(scoreManager.getScore(), scoreManager.getStarsEarned(), scoreManager.getStarProgress());
            listener.onShotsUpdated(endlessWaveCount);
        }

        updateTrajectory();
    }

    private List<Bubble> generateEndlessRow() {
        if (pregeneratedRowQueue.size() < 2) {
            int nextParity = grid.getRowParity() ^ 1;
            if (!pregeneratedRowQueue.isEmpty()) {
                nextParity ^= (pregeneratedRowQueue.size() % 2);
            }
            List<BubbleColor> activeColors = EndlessPatternGenerator.getActiveColors(endlessWaveCount, endlessColorsPool);
            List<List<Bubble>> nextChunk = EndlessPatternGenerator.generateMultiRowChunk(
                    endlessWaveCount + pregeneratedRowQueue.size() + 1,
                    nextParity,
                    3,
                    activeColors,
                    random
            );
            pregeneratedRowQueue.addAll(nextChunk);
        }

        return pregeneratedRowQueue.pollFirst();
    }

    private BubbleColor pickRandomColor() {
        return pickSmartLauncherColor(currentBubble != null ? currentBubble.getColor() : null);
    }

    private BubbleColor pickSmartLauncherColor(BubbleColor avoidColorIfPossible) {
        // 1. Identify all active normal bubbles on the board
        List<Bubble> allBubbles = new ArrayList<>();
        for (Bubble b : grid.getAllBubbles()) {
            if (b != null && b.getType() == BubbleType.NORMAL && b.getColor() != BubbleColor.NONE) {
                allBubbles.add(b);
            }
        }

        if (allBubbles.isEmpty()) {
            if (isEndlessMode) {
                List<BubbleColor> active = EndlessPatternGenerator.getActiveColors(endlessWaveCount, endlessColorsPool);
                return !active.isEmpty() ? active.get(random.nextInt(active.size())) : BubbleColor.RED;
            }
            if (currentLevel != null && !currentLevel.getAvailableColors().isEmpty()) {
                return currentLevel.getAvailableColors().get(random.nextInt(currentLevel.getAvailableColors().size()));
            }
            return BubbleColor.RED;
        }

        // 2. Identify exposed bottom frontier bubbles (bubbles with at least one open neighbor or lowest in column)
        List<Bubble> frontierBubbles = new ArrayList<>();
        Bubble lowestDangerBubble = null;
        float maxDangerY = -1f;

        for (Bubble b : allBubbles) {
            GridPosition pos = b.getGridPosition();
            if (pos == null) continue;

            // Check if lowest in column or has empty downward neighbor
            boolean isBottomExposed = false;
            List<GridPosition> neighbors = NeighborCalculator.getNeighbors(pos, grid.getRowParity());
            for (GridPosition n : neighbors) {
                if (n.row > pos.row && grid.getBubble(n) == null) {
                    isBottomExposed = true;
                    break;
                }
            }
            if (pos.row == BubbleGrid.MAX_ROWS - 1 || isBottomExposed) {
                frontierBubbles.add(b);
            }

            // Check if in danger zone (within 3.5 radii of danger line)
            if (deadlineY > 0 && (b.getY() + b.getRadius()) >= (deadlineY - bubbleRadius * 3.5f)) {
                if (b.getY() > maxDangerY) {
                    maxDangerY = b.getY();
                    lowestDangerBubble = b;
                }
            }
        }

        if (frontierBubbles.isEmpty()) {
            frontierBubbles = allBubbles;
        }

        // 3. Priority 1: If in critical danger zone, 75% chance to give the exact danger bubble's color!
        if (lowestDangerBubble != null && random.nextInt(100) < 75) {
            return lowestDangerBubble.getColor();
        }

        // 4. Priority 2: Look for match clusters on the exposed frontier (groups of 2+ connected same color)
        List<BubbleColor> matchableColors = new ArrayList<>();
        List<BubbleColor> frontierColors = new ArrayList<>();
        for (Bubble b : frontierBubbles) {
            BubbleColor c = b.getColor();
            if (!frontierColors.contains(c)) {
                frontierColors.add(c);
            }
            GridPosition pos = b.getGridPosition();
            if (pos != null) {
                for (GridPosition n : NeighborCalculator.getNeighbors(pos, grid.getRowParity())) {
                    Bubble nb = grid.getBubble(n);
                    if (nb != null && nb.getColor() == c && !matchableColors.contains(c)) {
                        matchableColors.add(c);
                    }
                }
            }
        }

        // 5. Select from matchable colors (high priority 70%), then frontier colors, then all board colors
        List<BubbleColor> candidatePool;
        if (!matchableColors.isEmpty() && random.nextInt(100) < 70) {
            candidatePool = matchableColors;
        } else if (!frontierColors.isEmpty()) {
            candidatePool = frontierColors;
        } else {
            candidatePool = new ArrayList<>();
            for (Bubble b : allBubbles) {
                if (!candidatePool.contains(b.getColor())) {
                    candidatePool.add(b.getColor());
                }
            }
        }

        // 6. If possible, pick a color different from avoidColorIfPossible (so current & next are versatile)
        if (avoidColorIfPossible != null && candidatePool.size() > 1) {
            List<BubbleColor> diversePool = new ArrayList<>(candidatePool);
            diversePool.remove(avoidColorIfPossible);
            if (!diversePool.isEmpty() && random.nextInt(100) < 80) {
                return diversePool.get(random.nextInt(diversePool.size()));
            }
        }

        return candidatePool.get(random.nextInt(candidatePool.size()));
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

        // Visual text feedback
        String boosterName = "BOOSTER!";
        int textColor = Color.parseColor("#FFD54F");
        if (type == BubbleType.BOMB) {
            boosterName = "BOMB!";
            textColor = Color.parseColor("#FF5722");
        } else if (type == BubbleType.RAINBOW) {
            boosterName = "RAINBOW!";
            textColor = Color.parseColor("#E040FB");
        } else if (type == BubbleType.LIGHTNING) {
            boosterName = "LIGHTNING!";
            textColor = Color.parseColor("#FFEB3B");
        } else if (type == BubbleType.FIREBALL) {
            boosterName = "FIREBALL!";
            textColor = Color.parseColor("#FF9800");
        }
        floatingTexts.add(new FloatingText(boosterName, launcherX, launcherY - bubbleRadius * 1.4f, textColor, 44f, 0.9f));

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

        // 2. Pick a new smart nextBubble and prepare it to pop into the preview position
        nextBubble.setColor(pickSmartLauncherColor(currentBubble.getColor()));
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

        dangerPulseTimer += dt;

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

        // 1. Update board bubbles (smooth sliding descent animation)
        for (Bubble b : grid.getAllBubbles()) {
            if (b != null) {
                b.update(dt);
            }
        }

        // 1.1 Update visual particles and texts
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

        if (isEndlessMode) {
            endlessWaveCount++;
            List<Bubble> newRow = generateEndlessRow();
            grid.shiftDownAndInsertRow(newRow);

            if (listener != null) {
                listener.onScoreUpdated(scoreManager.getScore(), scoreManager.getStarsEarned(), scoreManager.getStarProgress());
                listener.onShotsUpdated(endlessWaveCount);
            }
        } else {
            shotsRemaining--;
            if (listener != null) {
                listener.onScoreUpdated(scoreManager.getScore(), scoreManager.getStarsEarned(), scoreManager.getStarProgress());
                listener.onShotsUpdated(shotsRemaining);
            }
        }

        activeProjectile = null;
    }

    private void finishResolution() {
        if (isEndlessMode) {
            // If board is wiped clean in Endless Mode, give big bonus and refill top rows
            if (grid.getBubbleCount() == 0) {
                soundManager.playWin();
                confettiSystem.spawnCelebrationBurst(boardRight, boardBottom, 50);
                scoreManager.addScore(500);
                floatingTexts.add(new FloatingText("BOARD CLEARED! +500", (boardLeft + boardRight) * 0.5f, boardTop + bubbleRadius * 3, Color.parseColor("#FFD54F"), 52f, 1.5f));
                List<BubbleColor> activeColors = EndlessPatternGenerator.getActiveColors(endlessWaveCount, endlessColorsPool);
                EndlessPatternGenerator.populateInitialBoard(grid, 4, activeColors, random);
            }

            // Check danger line breach
            boolean touchedBottomLine = false;
            if (deadlineY > 0) {
                for (Bubble b : grid.getAllBubbles()) {
                    if (b != null && (b.getY() + b.getRadius()) >= (deadlineY - 2.0f)) {
                        touchedBottomLine = true;
                        break;
                    }
                }
            }

            if (touchedBottomLine) {
                state = GameState.LOSE;
                if (listener != null) {
                    listener.onGameLost(scoreManager.getScore(), "The bubbles breached the danger line!");
                }
                return;
            }

            state = GameState.READY;
            updateTrajectory();
            return;
        }

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
            int victoryBonus = scoreManager.addVictoryBonus(shotsRemaining);
            int finalScore = scoreManager.getScore();
            int starsEarned = Math.max(1, scoreManager.getStarsEarned());
            if (listener != null) {
                listener.onScoreUpdated(finalScore, starsEarned, scoreManager.getStarProgress());
                listener.onGameWon(finalScore, starsEarned);
            }
            return;
        }

        // Check lose condition 1: Bubbles crossed or touched bottom deadline line
        boolean touchedBottomLine = false;
        if (deadlineY > 0) {
            for (Bubble b : grid.getAllBubbles()) {
                if (b != null && (b.getY() + b.getRadius()) >= (deadlineY - 2.0f)) {
                    touchedBottomLine = true;
                    break;
                }
            }
        }

        if (touchedBottomLine) {
            state = GameState.LOSE;
            if (listener != null) {
                listener.onGameLost(scoreManager.getScore(), "Bubbles reached the danger line!");
            }
            return;
        }

        // Check lose condition 2: Out of shots
        if (shotsRemaining <= 0) {
            state = GameState.LOSE;
            if (listener != null) {
                listener.onGameLost(scoreManager.getScore(), "Out of shots! Don't give up!");
            }
            return;
        }

        state = GameState.READY;
        updateTrajectory();
    }

    public void draw(Canvas canvas, Paint paint) {
        // 0. Draw Bottom Danger Deadline
        drawDeadLine(canvas, paint);

        // 1. Draw Bold Shining Colored Laser Trajectory Line (ONLY when actively AIMING and not cancelled)
        if (state == GameState.AIMING && !isAimCancelled && trajectoryPoints != null && !trajectoryPoints.isEmpty()) {
            laserPath.rewind();
            laserPath.moveTo(launcherX, launcherY);
            for (PointF pt : trajectoryPoints) {
                laserPath.lineTo(pt.x, pt.y);
            }

            int laserColor = (currentBubble != null) ? currentBubble.getColor().primaryColor : Color.parseColor("#4FC3F7");
            int glowColor = (currentBubble != null) ? currentBubble.getColor().lightColor : Color.WHITE;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);

            // Layer 1: Broad Shining Outer Glow / Shadow
            paint.setColor(glowColor);
            paint.setAlpha(70);
            paint.setStrokeWidth(bubbleRadius * 0.48f);
            canvas.drawPath(laserPath, paint);

            // Layer 2: Bold Colored Laser Line
            paint.setColor(laserColor);
            paint.setAlpha(230);
            paint.setStrokeWidth(bubbleRadius * 0.22f);
            canvas.drawPath(laserPath, paint);

            // Layer 3: Intense White Core Beam
            paint.setColor(Color.WHITE);
            paint.setAlpha(255);
            paint.setStrokeWidth(bubbleRadius * 0.09f);
            canvas.drawPath(laserPath, paint);

            paint.setStyle(Paint.Style.FILL);
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

    private void drawDeadLine(Canvas canvas, Paint paint) {
        if (deadlineY <= 0) return;

        float lowestBubbleBottom = -1f;
        for (Bubble b : grid.getAllBubbles()) {
            if (b != null) {
                float bBottom = b.getY() + b.getRadius();
                if (bBottom > lowestBubbleBottom) {
                    lowestBubbleBottom = bBottom;
                }
            }
        }

        boolean inDanger = (lowestBubbleBottom > 0 && (deadlineY - lowestBubbleBottom) <= (bubbleRadius * 2.5f));

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        if (inDanger) {
            // Warning pulsation
            float pulse = (float) (0.55 + 0.45 * Math.sin(dangerPulseTimer * 10.0));

            // Outer warning glow
            paint.setPathEffect(null);
            paint.setColor(Color.parseColor("#FF1744"));
            paint.setAlpha((int) (pulse * 85));
            paint.setStrokeWidth(bubbleRadius * 0.38f);
            canvas.drawLine(boardLeft, deadlineY, boardRight, deadlineY, paint);

            // Dashed Danger Line
            paint.setPathEffect(dangerDashEffect != null ? dangerDashEffect : new DashPathEffect(new float[]{18f, 8f}, 0));
            paint.setColor(Color.parseColor("#FF5252"));
            paint.setAlpha((int) (160 + pulse * 95));
            paint.setStrokeWidth(4.5f);
            canvas.drawLine(boardLeft, deadlineY, boardRight, deadlineY, paint);
            paint.setPathEffect(null);

            // Small pulsing Danger Tag
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#FF5252"));
            paint.setAlpha((int) (180 + pulse * 75));
            paint.setTextSize(bubbleRadius * 0.34f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("⚠ DANGER LINE", (boardLeft + boardRight) * 0.5f, deadlineY - 8f, paint);
        } else {
            // Calm subtle dashed guideline
            paint.setPathEffect(normalDashEffect != null ? normalDashEffect : new DashPathEffect(new float[]{16f, 12f}, 0));
            paint.setColor(Color.WHITE);
            paint.setAlpha(45);
            paint.setStrokeWidth(2.5f);
            canvas.drawLine(boardLeft, deadlineY, boardRight, deadlineY, paint);
            paint.setPathEffect(null);
        }

        paint.setStyle(Paint.Style.FILL);
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

        // Preview Bubble & Current Bubble (drawn with natural depth during swap)
        if (nextBubble != null) {
            nextBubble.draw(canvas, paint);
        }
        if (currentBubble != null) {
            currentBubble.draw(canvas, paint);
        }

        // Swap Icon Indicator (pure clean curved rotation arrows without enclosing disc)
        float midX = (launcherX + previewX) / 2f;
        float midY = (launcherY + previewY) / 2f;
        drawSwapIcon(canvas, paint, midX, midY, bubbleRadius * 0.65f);
    }

    private void drawSwapIcon(Canvas canvas, Paint paint, float cx, float cy, float size) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        float strokeW = size * 0.13f;
        float rx = size * 0.72f;
        float ry = size * 0.42f;

        // Top curved arrow path (left to right with arrowhead on right)
        Path topPath = new Path();
        topPath.moveTo(cx - rx * 0.70f, cy - ry * 0.15f);
        topPath.cubicTo(cx - rx * 0.40f, cy - ry * 1.35f, cx + rx * 0.40f, cy - ry * 1.35f, cx + rx * 0.75f, cy - ry * 0.35f);
        // Arrowhead at (cx + rx * 0.75f, cy - ry * 0.35f)
        float headH = ry * 0.85f;
        float headW = rx * 0.42f;
        topPath.moveTo(cx + rx * 0.75f, cy - ry * 0.35f - headH);
        topPath.lineTo(cx + rx * 0.75f, cy - ry * 0.35f);
        topPath.lineTo(cx + rx * 0.75f - headW, cy - ry * 0.35f);

        // Bottom curved arrow path (right to left with arrowhead on left)
        Path botPath = new Path();
        botPath.moveTo(cx + rx * 0.70f, cy + ry * 0.15f);
        botPath.cubicTo(cx + rx * 0.40f, cy + ry * 1.35f, cx - rx * 0.40f, cy + ry * 1.35f, cx - rx * 0.75f, cy + ry * 0.35f);
        // Arrowhead at (cx - rx * 0.75f, cy + ry * 0.35f)
        botPath.moveTo(cx - rx * 0.75f, cy + ry * 0.35f + headH);
        botPath.lineTo(cx - rx * 0.75f, cy + ry * 0.35f);
        botPath.lineTo(cx - rx * 0.75f + headW, cy + ry * 0.35f);

        // 1. Dark Shadow Pass
        paint.setStrokeWidth(strokeW + 1.5f);
        paint.setColor(Color.argb(130, 0, 0, 0));
        canvas.save();
        canvas.translate(0, 2f);
        canvas.drawPath(topPath, paint);
        canvas.drawPath(botPath, paint);
        canvas.restore();

        // 2. Crisp White Foreground Pass
        paint.setStrokeWidth(strokeW);
        paint.setColor(Color.WHITE);
        canvas.drawPath(topPath, paint);
        canvas.drawPath(botPath, paint);

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

    public boolean isEndlessMode() {
        return isEndlessMode;
    }

    public int getEndlessWaveCount() {
        return endlessWaveCount;
    }

    public int getEndlessHighScore() {
        return endlessHighScore;
    }
}
