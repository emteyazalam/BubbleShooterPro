package com.redcodersgroup.bubbleshooter.game;

import com.redcodersgroup.bubbleshooter.board.BubbleGrid;
import com.redcodersgroup.bubbleshooter.board.GridPosition;
import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import com.redcodersgroup.bubbleshooter.bubble.BubbleColor;
import com.redcodersgroup.bubbleshooter.bubble.BubbleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EndlessPatternGenerator {

    private static final List<BubbleColor> DEFAULT_PALETTE = Arrays.asList(
            BubbleColor.RED,
            BubbleColor.BLUE,
            BubbleColor.GREEN,
            BubbleColor.YELLOW,
            BubbleColor.PURPLE,
            BubbleColor.ORANGE
    );

    /**
     * Returns an active color palette scaled by wave progression.
     * Starts with 3 colors for high matchability and gradually unlocks more colors.
     */
    public static List<BubbleColor> getActiveColors(int waveCount, List<BubbleColor> basePool) {
        List<BubbleColor> pool = (basePool != null && !basePool.isEmpty()) ? basePool : DEFAULT_PALETTE;
        int colorCount;
        if (waveCount <= 12) {
            colorCount = 3; // Waves 1-12: 3 colors (high matchability, great combo flow)
        } else if (waveCount <= 28) {
            colorCount = 4; // Waves 13-28: 4 colors
        } else if (waveCount <= 50) {
            colorCount = 5; // Waves 29-50: 5 colors
        } else {
            colorCount = Math.min(pool.size(), 6); // Wave 51+: full palette
        }
        colorCount = Math.min(colorCount, pool.size());

        List<BubbleColor> active = new ArrayList<>();
        for (int i = 0; i < colorCount; i++) {
            active.add(pool.get(i));
        }
        return active;
    }

    /**
     * Generates a single new top row using structured, playful patterns.
     */
    public static List<Bubble> generateRow(int waveCount, int cols, int rowParity, List<BubbleColor> activeColors, Random random) {
        if (activeColors == null || activeColors.isEmpty()) {
            activeColors = getActiveColors(waveCount, null);
        }

        BubbleColor[] rowColors = new BubbleColor[cols];
        BubbleType[] rowTypes = new BubbleType[cols];
        Arrays.fill(rowTypes, BubbleType.NORMAL);

        int patternType = random.nextInt(4);

        if (patternType == 0) {
            // Pattern 1: Cohesive Cluster Runs (groups of 2-4 same color)
            int c = 0;
            while (c < cols) {
                BubbleColor clusterColor = activeColors.get(random.nextInt(activeColors.size()));
                int clusterLen = 2 + random.nextInt(3); // 2 to 4 bubbles
                for (int i = 0; i < clusterLen && c < cols; i++, c++) {
                    rowColors[c] = clusterColor;
                }
            }
        } else if (patternType == 1) {
            // Pattern 2: Symmetrical Mirror
            int half = (cols + 1) / 2;
            BubbleColor[] halfColors = new BubbleColor[half];
            int h = 0;
            while (h < half) {
                BubbleColor colColor = activeColors.get(random.nextInt(activeColors.size()));
                int run = 1 + random.nextInt(2);
                for (int i = 0; i < run && h < half; i++, h++) {
                    halfColors[h] = colColor;
                }
            }
            for (int c = 0; c < cols; c++) {
                int srcIdx = (c < half) ? c : (cols - 1 - c);
                rowColors[c] = halfColors[srcIdx];
            }
        } else if (patternType == 2) {
            // Pattern 3: Alternating Pairs [A, A, B, B, A, A, ...]
            BubbleColor colorA = activeColors.get(random.nextInt(activeColors.size()));
            BubbleColor colorB = activeColors.get(random.nextInt(activeColors.size()));
            if (colorB == colorA && activeColors.size() > 1) {
                colorB = activeColors.get((activeColors.indexOf(colorA) + 1) % activeColors.size());
            }
            for (int c = 0; c < cols; c++) {
                rowColors[c] = ((c / 2) % 2 == 0) ? colorA : colorB;
            }
        } else {
            // Pattern 4: Solid Split with Center Accent
            BubbleColor leftColor = activeColors.get(random.nextInt(activeColors.size()));
            BubbleColor rightColor = activeColors.get(random.nextInt(activeColors.size()));
            int mid = cols / 2;
            for (int c = 0; c < cols; c++) {
                if (c < mid) {
                    rowColors[c] = leftColor;
                } else if (c > mid) {
                    rowColors[c] = rightColor;
                } else {
                    rowColors[c] = activeColors.get(random.nextInt(activeColors.size()));
                }
            }
        }

        // Occasional tactical booster spawn (Bomb or Rainbow)
        if (random.nextInt(100) < 6 || (waveCount > 0 && waveCount % 8 == 0 && random.nextBoolean())) {
            int boosterCol = (cols > 2) ? (1 + random.nextInt(cols - 2)) : 0;
            if (random.nextBoolean()) {
                rowTypes[boosterCol] = BubbleType.BOMB;
                rowColors[boosterCol] = BubbleColor.BOMB;
            } else {
                rowTypes[boosterCol] = BubbleType.RAINBOW;
                rowColors[boosterCol] = BubbleColor.RAINBOW;
            }
        }

        List<Bubble> result = new ArrayList<>(cols);
        for (int c = 0; c < cols; c++) {
            BubbleColor color = (rowColors[c] != null) ? rowColors[c] : activeColors.get(0);
            BubbleType type = rowTypes[c];
            result.add(new Bubble(color, type, new GridPosition(0, c)));
        }
        return result;
    }

    /**
     * Populates the starting board with structured, satisfying puzzle rows.
     */
    public static void populateInitialBoard(BubbleGrid grid, int rowCount, List<BubbleColor> activeColors, Random random) {
        grid.clear();
        if (activeColors == null || activeColors.isEmpty()) {
            activeColors = getActiveColors(1, null);
        }

        for (int r = 0; r < rowCount; r++) {
            int cols = grid.getCols(r);
            List<Bubble> rowBubbles = generateRow(1, cols, grid.getRowParity(), activeColors, random);
            for (int c = 0; c < cols; c++) {
                Bubble b = rowBubbles.get(c);
                b.setGridPosition(new GridPosition(r, c));
                grid.setBubble(r, c, b);
            }
        }
    }
}
