package com.lucky.truebond.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemePreferenceManager {
    private static final String PREF_NAME = "theme_preferences";
    private static final String KEY_IS_DARK_MODE = "is_dark_mode";
    
    private final SharedPreferences preferences;
    
    public ThemePreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public boolean isDarkMode() {
        return preferences.getBoolean(KEY_IS_DARK_MODE, false);
    }
    
    public void setDarkMode(boolean isDarkMode) {
        preferences.edit().putBoolean(KEY_IS_DARK_MODE, isDarkMode).apply();
    }
} 