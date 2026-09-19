package com.redcodersgroup.bubbleshooter.board;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class NeighborCalculatorTest {

    @Test
    public void testEvenRowNeighbors() {
        // Even row: row 2, col 3
        GridPosition pos = new GridPosition(2, 3);
        List<GridPosition> neighbors = NeighborCalculator.getNeighbors(pos);

        assertEquals(6, neighbors.size());
        assertTrue(neighbors.contains(new GridPosition(2, 2))); // Left
        assertTrue(neighbors.contains(new GridPosition(2, 4))); // Right
        assertTrue(neighbors.contains(new GridPosition(1, 2))); // Top-Left
        assertTrue(neighbors.contains(new GridPosition(1, 3))); // Top-Right
        assertTrue(neighbors.contains(new GridPosition(3, 2))); // Bottom-Left
        assertTrue(neighbors.contains(new GridPosition(3, 3))); // Bottom-Right
    }

    @Test
    public void testOddRowNeighbors() {
        // Odd row: row 1, col 2
        GridPosition pos = new GridPosition(1, 2);
        List<GridPosition> neighbors = NeighborCalculator.getNeighbors(pos);

        assertEquals(6, neighbors.size());
        assertTrue(neighbors.contains(new GridPosition(1, 1))); // Left
        assertTrue(neighbors.contains(new GridPosition(1, 3))); // Right
        assertTrue(neighbors.contains(new GridPosition(0, 2))); // Top-Left
        assertTrue(neighbors.contains(new GridPosition(0, 3))); // Top-Right
        assertTrue(neighbors.contains(new GridPosition(2, 2))); // Bottom-Left
        assertTrue(neighbors.contains(new GridPosition(2, 3))); // Bottom-Right
    }

    @Test
    public void testCeilingBoundary() {
        // Top-left corner: row 0, col 0
        GridPosition pos = new GridPosition(0, 0);
        List<GridPosition> neighbors = NeighborCalculator.getNeighbors(pos);

        // Cannot have negative rows or cols!
        for (GridPosition n : neighbors) {
            assertTrue("Row must be >= 0", n.row >= 0);
            assertTrue("Col must be >= 0", n.col >= 0);
        }
        assertTrue(neighbors.contains(new GridPosition(0, 1))); // Right
        assertTrue(neighbors.contains(new GridPosition(1, 0))); // Bottom-Right
    }
}
