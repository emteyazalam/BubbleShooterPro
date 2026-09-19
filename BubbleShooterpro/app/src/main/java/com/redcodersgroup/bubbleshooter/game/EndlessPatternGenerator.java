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
     * Returns an active color palette scaled for mid-level difficulty.
     * Starts with 4 colors immediately for rich tactical play, scaling to 5 colors at wave 5 and 6 colors at wave 16.
     */
    public static List<BubbleColor> getActiveColors(int waveCount, List<BubbleColor> basePool) {
        List<BubbleColor> pool = (basePool != null && !basePool.isEmpty()) ? basePool : DEFAULT_PALETTE;
        int colorCount;
        if (waveCount <= 4) {
            colorCount = 4; // Waves 1-4: 4 colors (balanced, mid-level challenge from start)
        } else if (waveCount <= 15) {
            colorCount = 5; // Waves 5-15: 5 colors (requires tactical planning & banking)
        } else {
            colorCount = Math.min(pool.size(), 6); // Wave 16+: full 6-color palette
        }
        colorCount = Math.min(colorCount, pool.size());

        List<BubbleColor> active = new ArrayList<>();
        for (int i = 0; i < colorCount; i++) {
            active.add(pool.get(i));
        }
        return active;
    }

    /**
     * Generates a single new top row using structured, mid-difficulty tactical patterns.
     */
    public static List<Bubble> generateRow(int waveCount, int cols, int rowParity, List<BubbleColor> activeColors, Random random) {
        if (activeColors == null || activeColors.isEmpty()) {
            activeColors = getActiveColors(waveCount, null);
        }

        BubbleColor[] rowColors = new BubbleColor[cols];
        BubbleType[] rowTypes = new BubbleType[cols];
        Arrays.fill(rowTypes, BubbleType.NORMAL);

        int patternType = random.nextInt(5);

        if (patternType == 0) {
            // Pattern 1: Balanced 2-Bubble Runs (groups of 2, requiring careful matching)
            int c = 0;
            while (c < cols) {
                BubbleColor clusterColor = activeColors.get(random.nextInt(activeColors.size()));
                int clusterLen = 2 + random.nextInt(2); // 2 to 3 bubbles
                for (int i = 0; i < clusterLen && c < cols; i++, c++) {
                    rowColors[c] = clusterColor;
                }
            }
        } else if (patternType == 1) {
            // Pattern 2: Symmetrical Mirror (tactical left/right symmetry)
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
            // Pattern 3: Checkerboard Alternating [A, B, A, B, A, B, ...]
            BubbleColor colorA = activeColors.get(random.nextInt(activeColors.size()));
            BubbleColor colorB = activeColors.get(random.nextInt(activeColors.size()));
            if (colorB == colorA && activeColors.size() > 1) {
                colorB = activeColors.get((activeColors.indexOf(colorA) + 1) % activeColors.size());
            }
            for (int c = 0; c < cols; c++) {
                rowColors[c] = (c % 2 == 0) ? colorA : colorB;
            }
        } else if (patternType == 3) {
            // Pattern 4: Multi-Section 4-Way Split (4 distinct color segments)
            BubbleColor[] segColors = new BubbleColor[4];
            for (int i = 0; i < 4; i++) {
                segColors[i] = activeColors.get((i + random.nextInt(activeColors.size())) % activeColors.size());
            }
            for (int c = 0; c < cols; c++) {
                int seg = Math.min(3, (c * 4) / cols);
                rowColors[c] = segColors[seg];
            }
        } else {
            // Pattern 5: Center Keystone with Contrasting Flanks
            BubbleColor flankColor = activeColors.get(random.nextInt(activeColors.size()));
            BubbleColor centerColor = activeColors.get(random.nextInt(activeColors.size()));
            if (centerColor == flankColor && activeColors.size() > 1) {
                centerColor = activeColors.get((activeColors.indexOf(flankColor) + 1) % activeColors.size());
            }
            int mid = cols / 2;
            for (int c = 0; c < cols; c++) {
                if (c == mid || c == mid - 1) {
                    rowColors[c] = centerColor;
                } else {
                    rowColors[c] = flankColor;
                }
            }
        }

        // Tactical Stone blocker obstacle (spawn starting wave 8+, moderate 7% chance)
        if (waveCount >= 8 && random.nextInt(100) < 7) {
            int stoneCol = 1 + random.nextInt(Math.max(1, cols - 2));
            rowTypes[stoneCol] = BubbleType.STONE;
            rowColors[stoneCol] = BubbleColor.STONE;
        }

        // Tactical booster spawn (Bomb or Rainbow, moderate 5% chance)
        if (random.nextInt(100) < 5 || (waveCount > 0 && waveCount % 7 == 0 && random.nextBoolean())) {
            int boosterCol = (cols > 2) ? (1 + random.nextInt(cols - 2)) : 0;
            if (rowTypes[boosterCol] == BubbleType.NORMAL) {
                if (random.nextBoolean()) {
                    rowTypes[boosterCol] = BubbleType.BOMB;
                    rowColors[boosterCol] = BubbleColor.BOMB;
                } else {
                    rowTypes[boosterCol] = BubbleType.RAINBOW;
                    rowColors[boosterCol] = BubbleColor.RAINBOW;
                }
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
     * Pre-generates a coherent 2-3 row chunk with 2D vertical & diagonal mid-level puzzle patterns.
     */
    public static List<List<Bubble>> generateMultiRowChunk(int waveCount, int startParity, int numRows, List<BubbleColor> activeColors, Random random) {
        if (activeColors == null || activeColors.isEmpty()) {
            activeColors = getActiveColors(waveCount, null);
        }

        List<List<Bubble>> chunk = new ArrayList<>(numRows);
        int currentParity = startParity;
        int motif = random.nextInt(5);

        if (motif == 0) {
            // Motif 1: Hexagonal 2D Checkerboard & Honeycomb Weave
            BubbleColor colA = activeColors.get(random.nextInt(activeColors.size()));
            BubbleColor colB = activeColors.get((activeColors.indexOf(colA) + 1) % activeColors.size());
            BubbleColor colC = (activeColors.size() > 2)
                    ? activeColors.get((activeColors.indexOf(colB) + 1) % activeColors.size())
                    : colA;

            for (int r = 0; r < numRows; r++) {
                int cols = (currentParity == 0) ? BubbleGrid.COLS_EVEN : BubbleGrid.COLS_ODD;
                BubbleColor[] rowColors = new BubbleColor[cols];
                for (int c = 0; c < cols; c++) {
                    int slot = (c + (r % 2) * 2) % 3;
                    if (slot == 0) rowColors[c] = colA;
                    else if (slot == 1) rowColors[c] = colB;
                    else rowColors[c] = colC;
                }

                BubbleType[] rowTypes = new BubbleType[cols];
                Arrays.fill(rowTypes, BubbleType.NORMAL);

                // Occasional mid-level tactical obstacle on later waves
                if (waveCount >= 10 && r == 0 && random.nextInt(100) < 10) {
                    int stoneCol = cols / 2;
                    rowTypes[stoneCol] = BubbleType.STONE;
                    rowColors[stoneCol] = BubbleColor.STONE;
                }

                List<Bubble> row = new ArrayList<>(cols);
                for (int c = 0; c < cols; c++) {
                    row.add(new Bubble(rowColors[c], rowTypes[c], new GridPosition(0, c)));
                }
                chunk.add(row);
                currentParity ^= 1;
            }
        } else if (motif == 1) {
            // Motif 2: Diagonal Striped Chevron Ribbons (width 2 bands)
            BubbleColor colorA = activeColors.get(random.nextInt(activeColors.size()));
            BubbleColor colorB = activeColors.get(random.nextInt(activeColors.size()));
            if (colorB == colorA && activeColors.size() > 1) {
                colorB = activeColors.get((activeColors.indexOf(colorA) + 1) % activeColors.size());
            }

            for (int r = 0; r < numRows; r++) {
                int cols = (currentParity == 0) ? BubbleGrid.COLS_EVEN : BubbleGrid.COLS_ODD;
                List<Bubble> row = new ArrayList<>(cols);
                for (int c = 0; c < cols; c++) {
                    BubbleColor col = (((c + r) / 2) % 2 == 0) ? colorA : colorB;
                    row.add(new Bubble(col, BubbleType.NORMAL, new GridPosition(0, c)));
                }
                chunk.add(row);
                currentParity ^= 1;
            }
        } else if (motif == 2) {
            // Motif 3: Symmetrical Concentric Arcs with Keystone Center
            BubbleColor outer = activeColors.get(random.nextInt(activeColors.size()));
            BubbleColor inner = activeColors.get((activeColors.indexOf(outer) + 1) % activeColors.size());

            for (int r = 0; r < numRows; r++) {
                int cols = (currentParity == 0) ? BubbleGrid.COLS_EVEN : BubbleGrid.COLS_ODD;
                List<Bubble> row = new ArrayList<>(cols);
                int border = Math.max(1, 2 - r);
                for (int c = 0; c < cols; c++) {
                    boolean isOuter = (c < border || c >= (cols - border));
                    BubbleColor col = isOuter ? outer : inner;
                    BubbleType type = BubbleType.NORMAL;
                    if (!isOuter && r == numRows - 1 && c == cols / 2 && random.nextInt(100) < 12) {
                        type = BubbleType.BOMB;
                        col = BubbleColor.BOMB;
                    }
                    row.add(new Bubble(col, type, new GridPosition(0, c)));
                }
                chunk.add(row);
                currentParity ^= 1;
            }
        } else if (motif == 3) {
            // Motif 4: Segmented 4-Column Interlocking Blocks
            BubbleColor c1 = activeColors.get(random.nextInt(activeColors.size()));
            BubbleColor c2 = activeColors.get((activeColors.indexOf(c1) + 1) % activeColors.size());
            BubbleColor c3 = (activeColors.size() > 2) ? activeColors.get((activeColors.indexOf(c2) + 1) % activeColors.size()) : c1;
            BubbleColor c4 = (activeColors.size() > 3) ? activeColors.get((activeColors.indexOf(c3) + 1) % activeColors.size()) : c2;

            BubbleColor[] quad = new BubbleColor[]{c1, c2, c3, c4};

            for (int r = 0; r < numRows; r++) {
                int cols = (currentParity == 0) ? BubbleGrid.COLS_EVEN : BubbleGrid.COLS_ODD;
                List<Bubble> row = new ArrayList<>(cols);
                for (int c = 0; c < cols; c++) {
                    int quadIdx = ((c * 4) / cols + r) % 4;
                    row.add(new Bubble(quad[quadIdx], BubbleType.NORMAL, new GridPosition(0, c)));
                }
                chunk.add(row);
                currentParity ^= 1;
            }
        } else {
            // Motif 5: Harmonious Tactical Cluster Runs
            for (int r = 0; r < numRows; r++) {
                int cols = (currentParity == 0) ? BubbleGrid.COLS_EVEN : BubbleGrid.COLS_ODD;
                List<Bubble> row = generateRow(waveCount + r, cols, currentParity, activeColors, random);
                chunk.add(row);
                currentParity ^= 1;
            }
        }

        return chunk;
    }

    /**
     * Populates the starting board with structured, mid-difficulty puzzle rows.
     */
    public static void populateInitialBoard(BubbleGrid grid, int rowCount, List<BubbleColor> activeColors, Random random) {
        grid.clear();
        if (activeColors == null || activeColors.isEmpty()) {
            activeColors = getActiveColors(1, null);
        }

        List<List<Bubble>> initialChunk = generateMultiRowChunk(1, 0, rowCount, activeColors, random);
        for (int r = 0; r < initialChunk.size() && r < rowCount; r++) {
            List<Bubble> rowBubbles = initialChunk.get(r);
            int cols = grid.getCols(r);
            for (int c = 0; c < rowBubbles.size() && c < cols; c++) {
                Bubble b = rowBubbles.get(c);
                b.setGridPosition(new GridPosition(r, c));
                grid.setBubble(r, c, b);
            }
        }
    }
}
