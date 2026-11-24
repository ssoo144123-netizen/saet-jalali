package com.example.desktopclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String[] OPT_KEYS = {"h24", "faDigits", "showSec", "speakHour", "speakAlarm"};
    private static final String[] OPT_NAMES = {
        "فرمت ۲۴ ساعته", "ارقام فارسی", "نمایش ثانیه", "گفتن ساعت هر ساعت", "گفتن ساعت هنگام آلارم"
    };
    private static final boolean[] OPT_DEFAULTS = {true, true, true, false, true};

    private final Handler handler = new Handler();
    private final List<Alarm> alarms = new ArrayList<>();
    private ScrollView scroll;
    private LinearLayout alarmsBox;
    private TextView timeView, dateView, nextView;
    private AnalogClockView analog;
    private TextToSpeech tts;
    private int lastSpokenHour = -1;
    private int fg, accent;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("ساعت دسکتاپی");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS && tts.setLanguage(new Locale("fa")) < 0)
                tts.setLanguage(Locale.getDefault());
        });

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = Fmt.dp(this, 16);
        root.setPadding(pad, pad, pad, pad);

        timeView = new TextView(this);
        timeView.setTextSize(52);
        timeView.setTypeface(Typeface.DEFAULT_BOLD);
        timeView.setGravity(Gravity.CENTER);

        dateView = new TextView(this);
        dateView.setTextSize(16);
        dateView.setGravity(Gravity.CENTER);

        analog = new AnalogClockView(this);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(Fmt.dp(this, 240), Fmt.dp(this, 240));
        alp.topMargin = Fmt.dp(this, 12);
        alp.bottomMargin = Fmt.dp(this, 8);

        nextView = new TextView(this);
        nextView.setTextSize(14);
        nextView.setGravity(Gravity.CENTER);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        Button add = new Button(this);
        add.setText("＋ آلارم");
        add.setOnClickListener(v -> addAlarmFlow());
        Button settings = new Button(this);
        settings.setText("تنظیمات");
        settings.setOnClickListener(v -> showSettings());
        Button theme = new Button(this);
        theme.setText("تم");
        theme.setOnClickListener(v -> showThemes());
        buttons.addView(add);
        buttons.addView(settings);
        buttons.addView(theme);

        alarmsBox = new LinearLayout(this);
        alarmsBox.setOrientation(LinearLayout.VERTICAL);

        root.addView(timeView);
        root.addView(dateView);
        root.addView(analog, alp);
        root.addView(nextView);
        root.addView(buttons);
        root.addView(alarmsBox, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);

        AlarmScheduler.scheduleAll(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarms.clear();
        alarms.addAll(AlarmStore.load(this));
        applyTheme();
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(tick);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    private boolean faDigits() { return Settings.getBool(this, "faDigits", true); }

    private void applyTheme() {
        int idx = Math.min(Settings.theme(this), Settings.THEME_COLORS.length - 1);
        int[] t = Settings.THEME_COLORS[idx];
        fg = t[0];
        accent = t[2];
        scroll.setBackgroundColor(t[1]);
        getWindow().setStatusBarColor(t[1]);
        timeView.setTextColor(fg);
        dateView.setTextColor(fg);
        nextView.setTextColor(accent);
        reloadList();
    }

    private void refresh() {
        Calendar now = Calendar.getInstance();
        boolean h24 = Settings.getBool(this, "h24", true);
        boolean showSec = Settings.getBool(this, "showSec", true);
        boolean fa = faDigits();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int min = now.get(Calendar.MINUTE);
        int sec = now.get(Calendar.SECOND);
        int shownHour = h24 ? hour : (hour % 12 == 0 ? 12 : hour % 12);

        String t = String.format(Locale.US, "%02d:%02d", shownHour, min);
        if (showSec) t += String.format(Locale.US, ":%02d", sec);
        if (!h24) t += now.get(Calendar.AM_PM) == Calendar.AM ? " ق.ظ" : " ب.ظ";
        timeView.setText(Fmt.digits(t, fa));

        JalaliCalendar j = JalaliCalendar.fromGregorian(
                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
        String weekday = JalaliCalendar.WEEKDAYS[now.get(Calendar.DAY_OF_WEEK) - 1];
        dateView.setText(Fmt.digits(weekday + "  " + j.format(), fa));

        analog.configure(fg, accent, fa, showSec);

        // آلارم بعدی
        Alarm next = null;
        long nextAt = -1;
        for (Alarm a : alarms) {
            if (!a.on) continue;
            long tt = a.nextTrigger(now.getTimeInMillis());
            if (tt > 0 && (nextAt < 0 || tt < nextAt)) { nextAt = tt; next = a; }
        }
        if (next == null) {
            nextView.setText("آلارم فعالی وجود ندارد");
        } else {
            long diffMin = (nextAt - now.getTimeInMillis()) / 60000L;
            String txt = String.format(Locale.US, "آلارم بعدی: %02d:%02d  (%d ساعت و %d دقیقه دیگر)",
                    next.hour, next.minute, diffMin / 60, diffMin % 60);
            nextView.setText(Fmt.digits(txt, fa));
        }

        // گفتن ساعت هر ساعت (وقتی برنامه باز است)
        if (Settings.getBool(this, "speakHour", false) && min == 0 && sec == 0 && lastSpokenHour != hour) {
            lastSpokenHour = hour;
            String say = "ساعت " + shownHour + (min == 0 ? "" : " و " + min + " دقیقه");
            tts.speak(say, TextToSpeech.QUEUE_FLUSH, null, "hour");
        }
    }

    private void reloadList() {
        alarmsBox.removeAllViews();
        boolean fa = faDigits();
        if (alarms.isEmpty()) {
            TextView e = new TextView(this);
            e.setText("آلارمی تعریف نشده است");
            e.setTextColor(fg);
            e.setGravity(Gravity.CENTER);
            alarmsBox.addView(e);
            return;
        }
        for (int i = 0; i < alarms.size(); i++) {
            final int idx = i;
            TextView row = new TextView(this);
            row.setText(alarms.get(i).summary(fa));
            row.setTextColor(fg);
            row.setTextSize(16);
            int p = Fmt.dp(this, 10);
            row.setPadding(p, p, p, p);
            row.setOnClickListener(v -> toggleAlarm(idx));
            row.setOnLongClickListener(v -> { confirmDelete(idx); return true; });
            alarmsBox.addView(row);
        }
    }

    private void toggleAlarm(int idx) {
        Alarm a = alarms.get(idx);
        a.on = !a.on;
        AlarmStore.save(this, alarms);
        if (a.on) AlarmScheduler.schedule(this, a);
        else AlarmScheduler.cancel(this, a);
        reloadList();
    }

    private void confirmDelete(int idx) {
        new AlertDialog.Builder(this)
                .setTitle("حذف آلارم؟")
                .setPositiveButton("حذف", (d, w) -> {
                    Alarm a = alarms.remove(idx);
                    AlarmScheduler.cancel(this, a);
                    AlarmStore.save(this, alarms);
                    reloadList();
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void addAlarmFlow() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hh, mm) -> askLabel(hh, mm),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void askLabel(int hh, int mm) {
        final EditText input = new EditText(this);
        input.setHint("برچسب (اختیاری)");
        new AlertDialog.Builder(this)
                .setTitle("برچسب آلارم")
                .setView(input)
                .setPositiveButton("ادامه", (d, w) -> askDays(hh, mm, input.getText().toString().trim()))
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void askDays(int hh, int mm, String label) {
        final String[] names = new String[7];
        final boolean[] chk = new boolean[7];
        for (int i = 0; i < 7; i++) names[i] = JalaliCalendar.WEEKDAYS[i];
        new AlertDialog.Builder(this)
                .setTitle("روزهای تکرار (بدون انتخاب = یک‌بار)")
                .setMultiChoiceItems(names, chk, (d, which, checked) -> chk[which] = checked)
                .setPositiveButton("ذخیره", (d, w) -> {
                    int mask = 0;
                    for (int i = 0; i < 7; i++) if (chk[i]) mask |= 1 << i;
                    createAlarm(hh, mm, label, mask);
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void createAlarm(int hh, int mm, String label, int mask) {
        Alarm a = new Alarm();
        a.id = System.currentTimeMillis();
        a.hour = hh;
        a.minute = mm;
        a.label = label;
        a.days = mask;
        a.on = true;
        alarms.add(a);
        AlarmStore.save(this, alarms);
        AlarmScheduler.schedule(this, a);
        reloadList();
    }

    private void showSettings() {
        final boolean[] vals = new boolean[OPT_KEYS.length];
        for (int i = 0; i < OPT_KEYS.length; i++)
            vals[i] = Settings.getBool(this, OPT_KEYS[i], OPT_DEFAULTS[i]);
        new AlertDialog.Builder(this)
                .setTitle("تنظیمات")
                .setMultiChoiceItems(OPT_NAMES, vals, (d, which, checked) -> vals[which] = checked)
                .setPositiveButton("ذخیره", (d, w) -> {
                    for (int i = 0; i < OPT_KEYS.length; i++)
                        Settings.setBool(this, OPT_KEYS[i], vals[i]);
                    refresh();
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void showThemes() {
        new AlertDialog.Builder(this)
                .setTitle("تم")
                .setSingleChoiceItems(Settings.THEME_NAMES, Settings.theme(this), (d, which) -> {
                    Settings.setTheme(this, which);
                    applyTheme();
                    d.dismiss();
                })
                .show();
    }
}
