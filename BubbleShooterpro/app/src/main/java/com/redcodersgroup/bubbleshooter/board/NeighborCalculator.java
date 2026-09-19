package com.redcodersgroup.bubbleshooter.board;

import java.util.ArrayList;
import java.util.List;

public class NeighborCalculator {
    public static final int COLS_EVEN = 8;
    public static final int COLS_ODD = 7;
    public static final int MAX_ROWS = 24;

    public static boolean isValidPosition(int row, int col) {
        if (row < 0 || row >= MAX_ROWS || col < 0) {
            return false;
        }
        int maxCols = (row % 2 == 0) ? COLS_EVEN : COLS_ODD;
        return col < maxCols;
    }

    public static boolean isValidPosition(GridPosition pos) {
        if (pos == null) return false;
        return isValidPosition(pos.row, pos.col);
    }

    public static List<GridPosition> getNeighbors(GridPosition pos) {
        List<GridPosition> neighbors = new ArrayList<>(6);
        if (pos == null) return neighbors;

        int r = pos.row;
        int c = pos.col;

        // Left and Right
        addIfValid(neighbors, r, c - 1);
        addIfValid(neighbors, r, c + 1);

        if (r % 2 == 0) {
            // Even row
            addIfValid(neighbors, r - 1, c - 1);
            addIfValid(neighbors, r - 1, c);
            addIfValid(neighbors, r + 1, c - 1);
            addIfValid(neighbors, r + 1, c);
        } else {
            // Odd row
            addIfValid(neighbors, r - 1, c);
            addIfValid(neighbors, r - 1, c + 1);
            addIfValid(neighbors, r + 1, c);
            addIfValid(neighbors, r + 1, c + 1);
        }

        return neighbors;
    }

    private static void addIfValid(List<GridPosition> list, int row, int col) {
        if (isValidPosition(row, col)) {
            list.add(new GridPosition(row, col));
        }
    }
}
