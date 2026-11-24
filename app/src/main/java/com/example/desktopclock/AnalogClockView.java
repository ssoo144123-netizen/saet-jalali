package com.example.desktopclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.Calendar;

public class AnalogClockView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int fore = Color.WHITE, accent = Color.CYAN;
    private boolean fa = true, showSec = true;

    public AnalogClockView(Context ctx) { super(ctx); }

    public void configure(int fore, int accent, boolean fa, boolean showSec) {
        this.fore = fore;
        this.accent = accent;
        this.fa = fa;
        this.showSec = showSec;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float r = Math.min(w, h) / 2f - 8f;
        if (r < 20f) return;
        float cx = w / 2f, cy = h / 2f;

        Calendar cal = Calendar.getInstance();
        float sec = cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000f;
        float min = cal.get(Calendar.MINUTE) + sec / 60f;
        float hr = cal.get(Calendar.HOUR) + min / 60f;

        // خط‌های ساعت و دقیقه
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(fore);
        paint.setAlpha(120);
        for (int i = 0; i < 60; i++) {
            double a = Math.toRadians(i * 6);
            float len = (i % 5 == 0) ? r * 0.1f : r * 0.05f;
            float sn = (float) Math.sin(a), cs = (float) Math.cos(a);
            canvas.drawLine(cx + sn * r, cy - cs * r, cx + sn * (r - len), cy - cs * (r - len), paint);
        }
        paint.setAlpha(255);

        // اعداد
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fore);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(r * 0.13f);
        paint.setFakeBoldText(true);
        float rn = r * 0.72f;
        for (int n = 1; n <= 12; n++) {
            double a = Math.toRadians(n * 30);
            float x = cx + (float) Math.sin(a) * rn;
            float y = cy - (float) Math.cos(a) * rn + r * 0.045f;
            canvas.drawText(Fmt.digits(String.valueOf(n), fa), x, y, paint);
        }
        paint.setFakeBoldText(false);

        // عقربه‌ها
        drawHand(canvas, cx, cy, hr * 30, r * 0.5f, Math.max(4f, r * 0.055f), fore);
        drawHand(canvas, cx, cy, min * 6, r * 0.78f, Math.max(3f, r * 0.035f), fore);
        if (showSec) drawHand(canvas, cx, cy, sec * 6, r * 0.88f, Math.max(1.5f, r * 0.018f), accent);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(accent);
        canvas.drawCircle(cx, cy, Math.max(4f, r * 0.045f), paint);
    }

    private void drawHand(Canvas c, float cx, float cy, float deg, float len, float width, int color) {
        double a = Math.toRadians(deg);
        float sn = (float) Math.sin(a), cs = (float) Math.cos(a);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(width);
        paint.setColor(color);
        c.drawLine(cx, cy, cx + sn * len, cy - cs * len, paint);
    }
}
