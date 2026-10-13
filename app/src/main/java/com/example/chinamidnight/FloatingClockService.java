package com.example.chinamidnight;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FloatingClockService extends Service {
    private WindowManager wm;
    private View bubble;
    private TextView times, countdown, countdownLabel;
    private final Handler handler = new Handler();
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (times != null) {
                long now = System.currentTimeMillis();
                String bd = fmt("Asia/Dhaka", now);
                String cn = fmt("Asia/Shanghai", now);
                long target = nextChinaMidnight();
                long d = Math.max(0, target-now);
                long h=d/3600000, m=(d/60000)%60, s=(d/1000)%60, ms=d%1000;
                times.setText("BD " + bd + "  ·  CN " + cn);
                countdown.setText(String.format(Locale.US,"%02d:%02d:%02d.%03d",h,m,s,ms));
                countdownLabel.setText("বাংলাদেশ রাত ১০টা / চীনের রাত ১২টা বাজতে বাকি");
            }
            handler.postDelayed(this, 50);
        }
    };

    private String fmt(String zone, long now) {
        SimpleDateFormat f = new SimpleDateFormat("hh:mm:ss.SS a", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone(zone));
        return f.format(new Date(now));
    }

    private long nextChinaMidnight() {
        java.util.Calendar c = java.util.Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(java.util.Calendar.HOUR_OF_DAY,0); c.set(java.util.Calendar.MINUTE,0);
        c.set(java.util.Calendar.SECOND,0); c.set(java.util.Calendar.MILLISECOND,0);
        if (c.getTimeInMillis() <= System.currentTimeMillis()) c.add(java.util.Calendar.DAY_OF_YEAR,1);
        return c.getTimeInMillis();
    }

    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        makeBubble();
        getSharedPreferences("floating_state", MODE_PRIVATE).edit().putBoolean("active", true).apply();
        scheduleMidnight();
        handler.post(tick);
    }

    private void makeBubble() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(9,6,9,7);

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.argb(238,25,27,34));
        shape.setCornerRadius(15f);
        shape.setStroke(1, Color.rgb(65,75,91));
        outer.setBackground(shape);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        times = new TextView(this);
        times.setTextColor(Color.WHITE);
        times.setTextSize(10);
        times.setTypeface(Typeface.MONOSPACE);
        times.setSingleLine(true);
        topRow.addView(times, new LinearLayout.LayoutParams(0,-2,1f));

        TextView close = new TextView(this);
        close.setText(" × ");
        close.setTextSize(17);
        close.setTextColor(Color.rgb(80,220,160));
        close.setPadding(3,0,0,0);
        topRow.addView(close);
        close.setOnClickListener(v -> stopSelf());
        outer.addView(topRow);

        LinearLayout timerBox = new LinearLayout(this);
        timerBox.setOrientation(LinearLayout.VERTICAL);
        timerBox.setGravity(Gravity.CENTER);
        timerBox.setPadding(7,4,7,4);
        GradientDrawable timerBg = new GradientDrawable();
        timerBg.setColor(Color.argb(210,38,44,57));
        timerBg.setCornerRadius(9f);
        timerBg.setStroke(1, Color.rgb(54,67,86));
        timerBox.setBackground(timerBg);
        LinearLayout.LayoutParams timerParams = new LinearLayout.LayoutParams(-1,-2);
        timerParams.topMargin = 5;
        outer.addView(timerBox, timerParams);

        countdown = new TextView(this);
        countdown.setTextColor(Color.rgb(130,183,255));
        countdown.setTextSize(12);
        countdown.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        countdown.setGravity(Gravity.CENTER);
        timerBox.addView(countdown);

        countdownLabel = new TextView(this);
        countdownLabel.setTextColor(Color.rgb(112,224,166));
        countdownLabel.setTextSize(9);
        countdownLabel.setGravity(Gravity.CENTER);
        countdownLabel.setText("বাংলাদেশ রাত ১০টা / চীনের রাত ১২টা বাজতে বাকি");
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1,-2);
        labelParams.topMargin = 2;
        outer.addView(countdownLabel, labelParams);

        final WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            android.os.Build.VERSION.SDK_INT >= 26 ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = 20; p.y = 180;

        outer.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY; float downX, downY;
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    startX=p.x; startY=p.y; downX=e.getRawX(); downY=e.getRawY(); return true;
                } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    p.x=startX+(int)(e.getRawX()-downX);
                    p.y=startY+(int)(e.getRawY()-downY);
                    try { wm.updateViewLayout(outer,p); } catch (Exception ignored) {}
                    return true;
                }
                return true;
            }
        });
        bubble = outer;
        wm.addView(bubble,p);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    private void scheduleMidnight() {
        try {
            android.app.AlarmManager am = (android.app.AlarmManager)getSystemService(ALARM_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) return;
            java.util.Calendar c = java.util.Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(java.util.Calendar.HOUR_OF_DAY, 0);
            c.set(java.util.Calendar.MINUTE, 0);
            c.set(java.util.Calendar.SECOND, 0);
            c.set(java.util.Calendar.MILLISECOND, 0);
            if (c.getTimeInMillis() <= System.currentTimeMillis()) c.add(java.util.Calendar.DAY_OF_YEAR, 1);
            Intent alarm = new Intent(this, MidnightReceiver.class);
            android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(this, 101, alarm,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
            if (android.os.Build.VERSION.SDK_INT >= 23) am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            else am.setExact(android.app.AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        } catch (Exception ignored) {}
    }

    private void cancelMidnightAlarm() {
        try {
            android.app.AlarmManager am = (android.app.AlarmManager)getSystemService(ALARM_SERVICE);
            Intent alarm = new Intent(this, MidnightReceiver.class);
            android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(this, 101, alarm,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
            am.cancel(pi);
            pi.cancel();
        } catch (Exception ignored) {}
    }

    @Override public void onDestroy() {
        getSharedPreferences("floating_state", MODE_PRIVATE).edit().putBoolean("active", false).apply();
        cancelMidnightAlarm();
        handler.removeCallbacks(tick);
        if (wm != null && bubble != null) try { wm.removeView(bubble); } catch (Exception ignored) {}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
