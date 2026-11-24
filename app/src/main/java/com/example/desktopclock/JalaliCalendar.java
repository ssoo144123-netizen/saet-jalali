package com.example.desktopclock;

/** تبدیل تاریخ میلادی به شمسی (جلالی) */
public final class JalaliCalendar {
    public static final String[] MONTHS = {
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    };
    // ایندکس = Calendar.DAY_OF_WEEK - 1  (یکشنبه = 0)
    public static final String[] WEEKDAYS = {
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    };

    public final int year, month, day;

    private JalaliCalendar(int y, int m, int d) { year = y; month = m; day = d; }

    /** gm: ماه میلادی 1..12 */
    public static JalaliCalendar fromGregorian(int gy, int gm, int gd) {
        int[] gdm = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        int gy2 = gm > 2 ? gy + 1 : gy;
        int days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gdm[gm - 1];
        int jy = -1595 + 33 * (days / 12053);
        days %= 12053;
        jy += 4 * (days / 1461);
        days %= 1461;
        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }
        int jm, jd;
        if (days < 186) {
            jm = 1 + days / 31;
            jd = 1 + days % 31;
        } else {
            jm = 7 + (days - 186) / 30;
            jd = 1 + (days - 186) % 30;
        }
        return new JalaliCalendar(jy, jm, jd);
    }

    public String format() {
        return day + " " + MONTHS[month - 1] + " " + year;
    }
}
