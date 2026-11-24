package com.example.desktopclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class AlarmScheduler {
    private AlarmScheduler() {}

    public static void scheduleAll(Context c) {
        for (Alarm a : AlarmStore.load(c)) schedule(c, a);
    }

    public static void schedule(Context c, Alarm a) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = pending(c, a.id);
        am.cancel(pi);
        if (!a.on) return;
        long t = a.nextTrigger(System.currentTimeMillis());
        if (t < 0) return;
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
        }
    }

    public static void cancel(Context c, Alarm a) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pending(c, a.id));
    }

    private static PendingIntent pending(Context c, long id) {
        Intent i = new Intent(c, AlarmReceiver.class);
        i.putExtra("id", id);
        return PendingIntent.getBroadcast(c, (int) (id ^ (id >>> 32)), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
