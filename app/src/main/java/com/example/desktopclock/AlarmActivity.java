package com.example.desktopclock;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class AlarmActivity extends Activity {
    private static final long AUTO_STOP_MS = 60_000L;

    private final Handler handler = new Handler();
    private final Runnable autoStop = this::finish;
    private MediaPlayer player;
    private Vibrator vibrator;
    private TextToSpeech tts;
    private String rawTime = "--:--";
    private String label = "آلارم";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        // نمایش روی صفحه قفل و روشن کردن صفحه (سازگار با اندروید ۶)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String t = getIntent().getStringExtra("time");
        String l = getIntent().getStringExtra("label");
        if (t != null) rawTime = t;
        if (l != null) label = l;

        boolean fa = Settings.getBool(this, "faDigits", true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(0xFFB71C1C);
        int pad = Fmt.dp(this, 32);
        root.setPadding(pad, pad, pad, pad);

        TextView tTime = new TextView(this);
        tTime.setText(Fmt.digits(rawTime, fa));
        tTime.setTextSize(64);
        tTime.setTextColor(0xFFFFFFFF);
        tTime.setGravity(Gravity.CENTER);

        TextView tLabel = new TextView(this);
        tLabel.setText(label);
        tLabel.setTextSize(24);
        tLabel.setTextColor(0xFFFFFFFF);
        tLabel.setGravity(Gravity.CENTER);

        Button stop = new Button(this);
        stop.setText("خاموش کردن");
        stop.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Fmt.dp(this, 40);

        root.addView(tTime);
        root.addView(tLabel);
        root.addView(stop, lp);
        setContentView(root);

        startSound();
        startVibration();
        if (Settings.getBool(this, "speakAlarm", true)) speak();

        handler.postDelayed(autoStop, AUTO_STOP_MS);
    }

    private void startSound() {
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setDataSource(this, uri);
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (Exception ignored) { }
    }

    private void startVibration() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) vibrator.vibrate(new long[]{0, 800, 600}, 0);
    }

    private void speak() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) return;
            if (tts.setLanguage(new Locale("fa")) < 0) tts.setLanguage(Locale.getDefault());
            tts.speak("آلارم. ساعت " + rawTime + ". " + label,
                    TextToSpeech.QUEUE_FLUSH, null, "alarm");
        });
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(autoStop);
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) { }
            player.release();
            player = null;
        }
        if (vibrator != null) vibrator.cancel();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
