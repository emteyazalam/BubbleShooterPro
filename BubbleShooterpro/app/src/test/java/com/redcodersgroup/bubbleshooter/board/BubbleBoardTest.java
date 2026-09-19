package com.redcodersgroup.bubbleshooter.board;

import com.redcodersgroup.bubbleshooter.bubble.Bubble;
import com.redcodersgroup.bubbleshooter.bubble.BubbleColor;
import com.redcodersgroup.bubbleshooter.bubble.BubbleType;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class BubbleBoardTest {

    private BubbleGrid grid;
    private BubbleBoard board;

    @Before
    public void setUp() {
        grid = new BubbleGrid();
        board = new BubbleBoard(grid);
    }

    @Test
    public void testMatchThreeSameColor() {
        // Place 3 RED bubbles adjacent to each other
        grid.setBubble(0, 0, new Bubble(BubbleColor.RED, new GridPosition(0, 0)));
        grid.setBubble(0, 1, new Bubble(BubbleColor.RED, new GridPosition(0, 1)));
        grid.setBubble(1, 0, new Bubble(BubbleColor.RED, new GridPosition(1, 0)));

        List<GridPosition> matches = board.findMatches(new GridPosition(0, 0));
        assertEquals(3, matches.size());
        assertTrue(matches.contains(new GridPosition(0, 0)));
        assertTrue(matches.contains(new GridPosition(0, 1)));
        assertTrue(matches.contains(new GridPosition(1, 0)));
    }

    @Test
    public void testMatchLessThanThreeReturnsEmpty() {
        // Place only 2 RED bubbles
        grid.setBubble(0, 0, new Bubble(BubbleColor.RED, new GridPosition(0, 0)));
        grid.setBubble(0, 1, new Bubble(BubbleColor.RED, new GridPosition(0, 1)));
        grid.setBubble(1, 0, new Bubble(BubbleColor.BLUE, new GridPosition(1, 0))); // Different color

        List<GridPosition> matches = board.findMatches(new GridPosition(0, 0));
        assertEquals(0, matches.size()); // Rule: requires 3 or more!
    }

    @Test
    public void testFloatingBubblesDetection() {
        // Row 0 has a ceiling bubble
        grid.setBubble(0, 0, new Bubble(BubbleColor.BLUE, new GridPosition(0, 0)));

        // Row 2 and 3 have disconnected bubbles (no connection to row 0)
        grid.setBubble(2, 2, new Bubble(BubbleColor.RED, new GridPosition(2, 2)));
        grid.setBubble(2, 3, new Bubble(BubbleColor.RED, new GridPosition(2, 3)));

        List<Bubble> floating = board.findFloatingBubbles();
        assertEquals(2, floating.size());

        // The ceiling bubble in (0, 0) should remain connected
        assertNotNull(grid.getBubble(0, 0));
        // Disconnected bubbles should have been detached
        assertNull(grid.getBubble(2, 2));
        assertNull(grid.getBubble(2, 3));
    }

    @Test
    public void testShiftDownAndInsertRow() {
        // Place bubbles in row 0
        grid.setBubble(0, 0, new Bubble(BubbleColor.BLUE, new GridPosition(0, 0)));
        grid.setBubble(0, 1, new Bubble(BubbleColor.BLUE, new GridPosition(0, 1)));
        assertEquals(0, grid.getRowParity());

        // Prepare new top row with 8 bubbles (since parity toggles to 1, row 0 will have 8 columns)
        java.util.List<Bubble> newTopRow = new java.util.ArrayList<>();
        for (int c = 0; c < BubbleGrid.COLS_ODD; c++) {
            newTopRow.add(new Bubble(BubbleColor.YELLOW, new GridPosition(0, c)));
        }

        grid.shiftDownAndInsertRow(newTopRow);

        // Parity is now 1 (odd)
        assertEquals(1, grid.getRowParity());

        // Row 0 should have the yellow bubbles
        for (int c = 0; c < BubbleGrid.COLS_ODD; c++) {
            Bubble b = grid.getBubble(0, c);
            assertNotNull(b);
            assertEquals(BubbleColor.YELLOW, b.getColor());
            assertEquals(0, b.getGridPosition().row);
            assertEquals(c, b.getGridPosition().col);
        }

        // Previous row 0 blue bubbles are now shifted to row 1
        Bubble b0 = grid.getBubble(1, 0);
        Bubble b1 = grid.getBubble(1, 1);
        assertNotNull(b0);
        assertNotNull(b1);
        assertEquals(BubbleColor.BLUE, b0.getColor());
        assertEquals(BubbleColor.BLUE, b1.getColor());
        assertEquals(1, b0.getGridPosition().row);
        assertEquals(0, b0.getGridPosition().col);
    }
}
