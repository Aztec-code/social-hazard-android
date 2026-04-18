package com.socialhazard.app.util;

import androidx.annotation.DrawableRes;

import com.socialhazard.app.R;

public final class AvatarCatalog {

    public static final String AVATAR_ALIVEPOOL = "alivepool";
    public static final String AVATAR_BOO_BEAR = "boo_bear";
    public static final String AVATAR_DROOLING_BIRD = "drooling_bird";
    public static final String AVATAR_MACHINE_GUN_SPONGE = "machine_gun_sponge";
    public static final String AVATAR_SONIC = "sonic";
    public static final String AVATAR_WHY = "why";

    private static final String[] IDS = new String[]{
            AVATAR_ALIVEPOOL,
            AVATAR_BOO_BEAR,
            AVATAR_DROOLING_BIRD,
            AVATAR_MACHINE_GUN_SPONGE,
            AVATAR_SONIC,
            AVATAR_WHY
    };

    private AvatarCatalog() {
    }

    public static String[] ids() {
        return IDS.clone();
    }

    public static String defaultAvatarId() {
        return IDS[0];
    }

    public static boolean isValid(String avatarId) {
        if (avatarId == null) {
            return false;
        }
        for (String id : IDS) {
            if (id.equals(avatarId)) {
                return true;
            }
        }
        return false;
    }

    public static int indexOf(String avatarId) {
        if (avatarId == null) {
            return 0;
        }
        for (int index = 0; index < IDS.length; index++) {
            if (IDS[index].equals(avatarId)) {
                return index;
            }
        }
        return 0;
    }

    public static String next(String avatarId) {
        int index = indexOf(avatarId);
        return IDS[(index + 1) % IDS.length];
    }

    public static String previous(String avatarId) {
        int index = indexOf(avatarId);
        return IDS[(index - 1 + IDS.length) % IDS.length];
    }

    @DrawableRes
    public static int drawableFor(String avatarId) {
        if (AVATAR_BOO_BEAR.equals(avatarId)) {
            return R.drawable.avatar_boo_bear;
        }
        if (AVATAR_DROOLING_BIRD.equals(avatarId)) {
            return R.drawable.avatar_drooling_bird;
        }
        if (AVATAR_MACHINE_GUN_SPONGE.equals(avatarId)) {
            return R.drawable.avatar_machine_gun_sponge;
        }
        if (AVATAR_SONIC.equals(avatarId)) {
            return R.drawable.avatar_sonic;
        }
        if (AVATAR_WHY.equals(avatarId)) {
            return R.drawable.avatar_why;
        }
        return R.drawable.avatar_alivepool;
    }
}
