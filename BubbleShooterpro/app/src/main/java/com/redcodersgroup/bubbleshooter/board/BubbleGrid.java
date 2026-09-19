package com.redcodersgroup.bubbleshooter.board;

import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import java.util.ArrayList;
import java.util.List;

public class BubbleGrid {
    public static final int COLS_EVEN = NeighborCalculator.COLS_EVEN;
    public static final int COLS_ODD = NeighborCalculator.COLS_ODD;
    public static final int MAX_ROWS = NeighborCalculator.MAX_ROWS;
    public static final float ROW_HEIGHT_RATIO = 1.7320508f; // sqrt(3)

    private final Bubble[][] grid;
    private float boardLeft = 0f;
    private float boardTop = 0f;
    private float bubbleRadius = 40f;

    public BubbleGrid() {
        this.grid = new Bubble[MAX_ROWS][COLS_EVEN];
    }

    public void setDimensions(float boardLeft, float boardTop, float bubbleRadius) {
        this.boardLeft = boardLeft;
        this.boardTop = boardTop;
        this.bubbleRadius = bubbleRadius;

        // Update coordinates of all existing bubbles in the grid
        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                Bubble b = grid[r][c];
                if (b != null) {
                    b.setX(getCenterX(r, c));
                    b.setY(getCenterY(r));
                    b.setRadius(bubbleRadius);
                }
            }
        }
    }

    public float getBubbleRadius() {
        return bubbleRadius;
    }

    public float getBoardLeft() {
        return boardLeft;
    }

    public float getBoardTop() {
        return boardTop;
    }

    public float getCenterX(int row, int col) {
        float xOffset = (row % 2 == 0) ? bubbleRadius : (bubbleRadius * 2f);
        return boardLeft + xOffset + (col * 2f * bubbleRadius);
    }

    public float getCenterY(int row) {
        return boardTop + bubbleRadius + (row * bubbleRadius * ROW_HEIGHT_RATIO);
    }

    public Bubble getBubble(int row, int col) {
        if (!NeighborCalculator.isValidPosition(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    public Bubble getBubble(GridPosition pos) {
        if (pos == null) return null;
        return getBubble(pos.row, pos.col);
    }

    public boolean setBubble(int row, int col, Bubble bubble) {
        if (!NeighborCalculator.isValidPosition(row, col)) {
            return false;
        }
        grid[row][col] = bubble;
        if (bubble != null) {
            bubble.setGridPosition(new GridPosition(row, col));
            bubble.setX(getCenterX(row, col));
            bubble.setY(getCenterY(row));
            bubble.setRadius(bubbleRadius);
        }
        return true;
    }

    public boolean setBubble(GridPosition pos, Bubble bubble) {
        if (pos == null) return false;
        return setBubble(pos.row, pos.col, bubble);
    }

    public Bubble removeBubble(int row, int col) {
        if (!NeighborCalculator.isValidPosition(row, col)) {
            return null;
        }
        Bubble b = grid[row][col];
        grid[row][col] = null;
        return b;
    }

    public Bubble removeBubble(GridPosition pos) {
        if (pos == null) return null;
        return removeBubble(pos.row, pos.col);
    }

    public boolean isEmpty(int row, int col) {
        return getBubble(row, col) == null;
    }

    public boolean isEmpty(GridPosition pos) {
        return getBubble(pos) == null;
    }

    public List<Bubble> getAllBubbles() {
        List<Bubble> list = new ArrayList<>();
        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != null) {
                    list.add(grid[r][c]);
                }
            }
        }
        return list;
    }

    public int getBubbleCount() {
        int count = 0;
        for (int r = 0; r < MAX_ROWS; r++) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getLowestOccupiedRow() {
        for (int r = MAX_ROWS - 1; r >= 0; r--) {
            int cols = (r % 2 == 0) ? COLS_EVEN : COLS_ODD;
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != null) {
                    return r;
                }
            }
        }
        return -1;
    }

    public void clear() {
        for (int r = 0; r < MAX_ROWS; r++) {
            for (int c = 0; c < COLS_EVEN; c++) {
                grid[r][c] = null;
            }
        }
    }
}
