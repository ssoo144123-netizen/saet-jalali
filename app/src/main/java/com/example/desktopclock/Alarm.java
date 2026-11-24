package com.example.desktopclock;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class Alarm {
    public long id;
    public boolean on = true;
    public int hour, minute;
    /** بیت i = روز i (یکشنبه=0). صفر یعنی فقط یک‌بار */
    public int days;
    public String label = "";

    /** زمان بعدی به میلی‌ثانیه، یا -1 اگر پیدا نشد */
    public long nextTrigger(long now) {
        for (int d = 0; d < 8; d++) {
            Calendar t = Calendar.getInstance();
            t.setTimeInMillis(now);
            t.add(Calendar.DAY_OF_YEAR, d);
            t.set(Calendar.HOUR_OF_DAY, hour);
            t.set(Calendar.MINUTE, minute);
            t.set(Calendar.SECOND, 0);
            t.set(Calendar.MILLISECOND, 0);
            if (t.getTimeInMillis() <= now) continue;
            if (days != 0 && (days & (1 << (t.get(Calendar.DAY_OF_WEEK) - 1))) == 0) continue;
            return t.getTimeInMillis();
        }
        return -1;
    }

    public String summary(boolean fa) {
        return (on ? "● " : "○ ")
                + Fmt.digits(String.format(Locale.US, "%02d:%02d", hour, minute), fa)
                + "   " + (label.isEmpty() ? "" : label + "   ")
                + "[" + daysText(days) + "]";
    }

    public static String daysText(int mask) {
        if (mask == 0) return "یک‌بار";
        if ((mask & 127) == 127) return "هر روز";
        StringBuilder sb = new StringBuilder();
        int[] order = {6, 0, 1, 2, 3, 4, 5};
        for (int d : order) {
            if ((mask & (1 << d)) != 0) {
                if (sb.length() > 0) sb.append("، ");
                sb.append(JalaliCalendar.WEEKDAYS[d]);
            }
        }
        return sb.toString();
    }

    public JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("id", id).put("on", on).put("h", hour)
                .put("m", minute).put("d", days).put("l", label);
    }

    public static Alarm fromJson(JSONObject o) throws JSONException {
        Alarm a = new Alarm();
        a.id = o.getLong("id");
        a.on = o.getBoolean("on");
        a.hour = o.getInt("h");
        a.minute = o.getInt("m");
        a.days = o.getInt("d");
        a.label = o.optString("l", "");
        return a;
    }
}
