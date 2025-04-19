package com.lucky.truebond;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.lucky.truebond.utils.ThemePreferenceManager;

public class TrueBondApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize theme
        ThemePreferenceManager themePreferenceManager = new ThemePreferenceManager(this);
        if (themePreferenceManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
} 