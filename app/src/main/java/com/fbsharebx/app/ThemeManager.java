package com.fbsharebx.app;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    public static void applySavedTheme(Context ctx) {
        // Dark mode is the only mode -- locked on for the aesthetic
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
