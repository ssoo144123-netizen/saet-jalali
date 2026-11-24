package com.example.desktopclock;

import android.content.Context;
import android.content.SharedPreferences;

public final class Settings {
    public static final String[] THEME_NAMES = {"تیره", "روشن", "آبی شب", "سبز نئونی", "طلایی", "بنفش"};
    /** {متن، پس‌زمینه، تأکید} */
    public static final int[][] THEME_COLORS = {
        {0xFFFFFFFF, 0xFF1E1E1E, 0xFF00D2FF},
        {0xFF202020, 0xFFF2F2F2, 0xFF007AFF},
        {0xFFE0F0FF, 0xFF0D1B3D, 0xFF00D2FF},
        {0xFF39FF14, 0xFF050A05, 0xFF39FF14},
        {0xFFFFD700, 0xFF1A1405, 0xFFFFB300},
        {0xFFF3E8FF, 0xFF3B1F66, 0xFFC084FC}
    };

    private static final String PREFS = "clock_settings";

    private Settings() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int theme(Context c) { return prefs(c).getInt("theme", 0); }
    public static void setTheme(Context c, int v) { prefs(c).edit().putInt("theme", v).apply(); }
    public static boolean getBool(Context c, String k, boolean def) { return prefs(c).getBoolean(k, def); }
    public static void setBool(Context c, String k, boolean v) { prefs(c).edit().putBoolean(k, v).apply(); }
}
