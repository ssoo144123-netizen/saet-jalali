package com.example.desktopclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        long id = intent.getLongExtra("id", -1);
        Alarm a = AlarmStore.find(c, id);
        if (a == null || !a.on) {
            AlarmScheduler.scheduleAll(c);
            return;
        }

        Intent ring = new Intent(c, AlarmActivity.class)
                .putExtra("time", String.format(Locale.US, "%02d:%02d", a.hour, a.minute))
                .putExtra("label", a.label.isEmpty() ? "آلارم" : a.label)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        c.startActivity(ring);

        if (a.days == 0) {          // یک‌بار: بعد از اجرا خاموش شود
            a.on = false;
            AlarmStore.update(c, a);
        }
        AlarmScheduler.scheduleAll(c);
    }
}
