package com.redcodersgroup.bubbleshooter.profile;

import com.redcodersgroup.bubbleshooter.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages predefined avatars and future Google Play Games profile integration.
 */
public class AvatarManager {

    public static final String DEFAULT_AVATAR_ID = "avatar_hero";
    public static final String DEFAULT_PLAYER_NAME = "Player";

    public static class AvatarItem {
        public final String id;
        public final String name;
        public final int drawableResId;

        public AvatarItem(String id, String name, int drawableResId) {
            this.id = id;
            this.name = name;
            this.drawableResId = drawableResId;
        }
    }

    private static final List<AvatarItem> PREDEFINED_AVATARS;

    static {
        List<AvatarItem> list = new ArrayList<>();
        list.add(new AvatarItem("avatar_hero", "Bubble Hero", R.drawable.ic_avatar_hero));
        list.add(new AvatarItem("avatar_princess", "Princess", R.drawable.ic_avatar_princess));
        list.add(new AvatarItem("avatar_cat", "Lucky Cat", R.drawable.ic_avatar_cat));
        list.add(new AvatarItem("avatar_panda", "Bao Panda", R.drawable.ic_avatar_panda));
        list.add(new AvatarItem("avatar_fox", "Rusty Fox", R.drawable.ic_avatar_fox));
        list.add(new AvatarItem("avatar_frog", "Hoppy Frog", R.drawable.ic_avatar_frog));
        list.add(new AvatarItem("avatar_penguin", "Pip Penguin", R.drawable.ic_avatar_penguin));
        list.add(new AvatarItem("avatar_wizard", "Star Mage", R.drawable.ic_avatar_wizard));
        PREDEFINED_AVATARS = Collections.unmodifiableList(list);
    }

    public static List<AvatarItem> getPredefinedAvatars() {
        return PREDEFINED_AVATARS;
    }

    public static int getAvatarDrawable(String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            return R.drawable.ic_avatar_hero;
        }
        for (AvatarItem item : PREDEFINED_AVATARS) {
            if (item.id.equalsIgnoreCase(avatarId)) {
                return item.drawableResId;
            }
        }
        return R.drawable.ic_avatar_hero;
    }

    public static AvatarItem getAvatarById(String avatarId) {
        if (avatarId == null || avatarId.isEmpty()) {
            return PREDEFINED_AVATARS.get(0);
        }
        for (AvatarItem item : PREDEFINED_AVATARS) {
            if (item.id.equalsIgnoreCase(avatarId)) {
                return item;
            }
        }
        return PREDEFINED_AVATARS.get(0);
    }
}
