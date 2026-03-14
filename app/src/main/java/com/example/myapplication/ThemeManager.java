package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_IS_DARK_MODE = "is_dark_mode";

    private SharedPreferences sharedPreferences;

    public ThemeManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setDarkMode(boolean isDarkMode) {
        sharedPreferences.edit().putBoolean(KEY_IS_DARK_MODE, isDarkMode).apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false);
    }

    public boolean toggleTheme() {
        boolean newMode = !isDarkMode();
        setDarkMode(newMode);
        return newMode;
    }

    public String getThemeName() {
        return isDarkMode() ? "🌙" : "☀️";
    }
}