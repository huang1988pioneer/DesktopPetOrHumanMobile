package com.pioneer.desktoppetorhumanmobile;

import android.content.Context;
import android.content.SharedPreferences;

/** Persists the small amount of state shared by the launcher and overlay service. */
final class PetPreferences {
    private static final String NAME = "desktop_pet";
    private static final String STAY_ON_TOP = "stay_on_top";
    private static final String CHARACTER_INDEX = "character_index";
    private static final String SIZE_LEVEL = "size_level";
    private static final String POSITION_X = "pos_x";
    private static final String POSITION_Y = "pos_y";

    private final SharedPreferences preferences;

    PetPreferences(Context context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    boolean isStayOnTop() {
        return preferences.getBoolean(STAY_ON_TOP, true);
    }

    void saveStayOnTop(boolean stayOnTop) {
        preferences.edit().putBoolean(STAY_ON_TOP, stayOnTop).apply();
    }

    int characterIndex() {
        return preferences.getInt(CHARACTER_INDEX, 0);
    }

    void saveCharacterIndex(int index) {
        preferences.edit().putInt(CHARACTER_INDEX, index).apply();
    }

    int sizeLevel(int defaultLevel) {
        return preferences.getInt(SIZE_LEVEL, defaultLevel);
    }

    void saveSizeLevel(int level) {
        preferences.edit().putInt(SIZE_LEVEL, level).apply();
    }

    boolean hasPosition() {
        return preferences.contains(POSITION_X) && preferences.contains(POSITION_Y);
    }

    int positionX() {
        return preferences.getInt(POSITION_X, 0);
    }

    int positionY() {
        return preferences.getInt(POSITION_Y, 0);
    }

    void savePosition(int x, int y) {
        preferences.edit()
                .putInt(POSITION_X, x)
                .putInt(POSITION_Y, y)
                .apply();
    }
}
