package com.redcodersgroup.bubbleshooter.store;

import com.redcodersgroup.bubbleshooter.ui.dialogs.HeartStoreDialog;
import org.junit.Test;
import static org.junit.Assert.*;

public class HeartStoreTest {

    @Test
    public void testHeartStorePricing() {
        assertEquals(5, HeartStoreDialog.COST_ONE_HEART);
        assertEquals(12, HeartStoreDialog.COST_TRIPLE_HEARTS);
        assertEquals(20, HeartStoreDialog.COST_FULL_REFILL);
    }

    @Test
    public void testAffordability() {
        assertTrue(StoreManager.canAfford(10, HeartStoreDialog.COST_ONE_HEART));
        assertTrue(StoreManager.canAfford(12, HeartStoreDialog.COST_TRIPLE_HEARTS));
        assertFalse(StoreManager.canAfford(11, HeartStoreDialog.COST_TRIPLE_HEARTS));
        assertTrue(StoreManager.canAfford(20, HeartStoreDialog.COST_FULL_REFILL));
        assertFalse(StoreManager.canAfford(19, HeartStoreDialog.COST_FULL_REFILL));
    }

    @Test
    public void testTripleHeartsDiscount() {
        // 3 single hearts would cost 3 * 5 = 15 diamonds. Triple pack costs 12 diamonds (20% discount).
        int separateCost = 3 * HeartStoreDialog.COST_ONE_HEART;
        assertTrue(HeartStoreDialog.COST_TRIPLE_HEARTS < separateCost);
    }

    @Test
    public void testFullRefillDiscount() {
        // 5 single hearts would cost 5 * 5 = 25 diamonds. Full refill costs 20 diamonds (20% discount).
        int separateCost = 5 * HeartStoreDialog.COST_ONE_HEART;
        assertTrue(HeartStoreDialog.COST_FULL_REFILL < separateCost);
    }
}
