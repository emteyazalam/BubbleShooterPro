package com.redcodersgroup.bubbleshooter.store;

import com.redcodersgroup.bubbleshooter.ui.dialogs.HeartStoreDialog;
import org.junit.Test;
import static org.junit.Assert.*;

public class HeartStoreTest {

    @Test
    public void testHeartStorePricing() {
        assertEquals(5, HeartStoreDialog.COST_ONE_HEART);
        assertEquals(20, HeartStoreDialog.COST_FULL_REFILL);
        assertEquals(40, HeartStoreDialog.COST_INFINITE_30_MIN);
    }

    @Test
    public void testAffordability() {
        assertTrue(StoreManager.canAfford(10, HeartStoreDialog.COST_ONE_HEART));
        assertTrue(StoreManager.canAfford(20, HeartStoreDialog.COST_FULL_REFILL));
        assertFalse(StoreManager.canAfford(19, HeartStoreDialog.COST_FULL_REFILL));
        assertTrue(StoreManager.canAfford(45, HeartStoreDialog.COST_INFINITE_30_MIN));
        assertFalse(StoreManager.canAfford(39, HeartStoreDialog.COST_INFINITE_30_MIN));
    }

    @Test
    public void testFullRefillDiscount() {
        // 5 single hearts would cost 5 * 5 = 25 diamonds. Full refill costs 20 diamonds (20% discount).
        int separateCost = 5 * HeartStoreDialog.COST_ONE_HEART;
        assertTrue(HeartStoreDialog.COST_FULL_REFILL < separateCost);
    }
}
