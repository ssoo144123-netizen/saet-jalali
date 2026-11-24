package com.example.desktopclock;

import android.content.Context;
import android.util.TypedValue;

public final class Fmt {
    private Fmt() {}

    /** تبدیل ارقام لاتین به فارسی */
    public static String digits(String s, boolean fa) {
        if (!fa) return s;
        StringBuilder b = new StringBuilder(s.length());
        for (char c : s.toCharArray())
            b.append(c >= '0' && c <= '9' ? (char) ('\u06F0' + (c - '0')) : c);
        return b.toString();
    }

    public static int dp(Context c, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }
}
