package com.redcodersgroup.bubbleshooter.board;

import java.util.ArrayList;
import java.util.List;

public class NeighborCalculator {
    public static final int COLS_EVEN = 9;
    public static final int COLS_ODD = 8;
    public static final int MAX_ROWS = 24;

    public static boolean isValidPosition(int row, int col) {
        return isValidPosition(row, col, 0);
    }

    public static boolean isValidPosition(int row, int col, int rowParity) {
        if (row < 0 || row >= MAX_ROWS || col < 0) {
            return false;
        }
        int maxCols = ((row + rowParity) % 2 == 0) ? COLS_EVEN : COLS_ODD;
        return col < maxCols;
    }

    public static boolean isValidPosition(GridPosition pos) {
        return isValidPosition(pos, 0);
    }

    public static boolean isValidPosition(GridPosition pos, int rowParity) {
        if (pos == null) return false;
        return isValidPosition(pos.row, pos.col, rowParity);
    }

    public static List<GridPosition> getNeighbors(GridPosition pos) {
        return getNeighbors(pos, 0);
    }

    public static List<GridPosition> getNeighbors(GridPosition pos, int rowParity) {
        List<GridPosition> neighbors = new ArrayList<>(6);
        if (pos == null) return neighbors;

        int r = pos.row;
        int c = pos.col;

        // Left and Right
        addIfValid(neighbors, r, c - 1, rowParity);
        addIfValid(neighbors, r, c + 1, rowParity);

        boolean isEven = ((r + rowParity) % 2 == 0);
        if (isEven) {
            // Even row
            addIfValid(neighbors, r - 1, c - 1, rowParity);
            addIfValid(neighbors, r - 1, c, rowParity);
            addIfValid(neighbors, r + 1, c - 1, rowParity);
            addIfValid(neighbors, r + 1, c, rowParity);
        } else {
            // Odd row
            addIfValid(neighbors, r - 1, c, rowParity);
            addIfValid(neighbors, r - 1, c + 1, rowParity);
            addIfValid(neighbors, r + 1, c, rowParity);
            addIfValid(neighbors, r + 1, c + 1, rowParity);
        }

        return neighbors;
    }

    private static void addIfValid(List<GridPosition> list, int row, int col, int rowParity) {
        if (isValidPosition(row, col, rowParity)) {
            list.add(new GridPosition(row, col));
        }
    }
}
