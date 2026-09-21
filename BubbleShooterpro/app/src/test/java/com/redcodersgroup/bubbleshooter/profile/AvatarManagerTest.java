package com.redcodersgroup.bubbleshooter.profile;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class AvatarManagerTest {

    @Test
    public void testPredefinedAvatarsCount() {
        List<AvatarManager.AvatarItem> avatars = AvatarManager.getPredefinedAvatars();
        assertNotNull(avatars);
        assertEquals(8, avatars.size());
    }

    @Test
    public void testGetAvatarById() {
        AvatarManager.AvatarItem hero = AvatarManager.getAvatarById("avatar_hero");
        assertNotNull(hero);
        assertEquals("avatar_hero", hero.id);
        assertEquals("Bubble Hero", hero.name);

        AvatarManager.AvatarItem princess = AvatarManager.getAvatarById("avatar_princess");
        assertNotNull(princess);
        assertEquals("avatar_princess", princess.id);

        AvatarManager.AvatarItem cat = AvatarManager.getAvatarById("avatar_cat");
        assertNotNull(cat);
        assertEquals("avatar_cat", cat.id);
    }

    @Test
    public void testGetAvatarById_fallback() {
        AvatarManager.AvatarItem fallbackNull = AvatarManager.getAvatarById(null);
        assertNotNull(fallbackNull);
        assertEquals("avatar_hero", fallbackNull.id);

        AvatarManager.AvatarItem fallbackUnknown = AvatarManager.getAvatarById("unknown_id_xyz");
        assertNotNull(fallbackUnknown);
        assertEquals("avatar_hero", fallbackUnknown.id);
    }

    @Test
    public void testGetAvatarDrawable_fallback() {
        int defaultRes = AvatarManager.getAvatarDrawable(null);
        assertTrue(defaultRes != 0);

        int invalidRes = AvatarManager.getAvatarDrawable("non_existent_avatar");
        assertEquals(defaultRes, invalidRes);
    }
}
