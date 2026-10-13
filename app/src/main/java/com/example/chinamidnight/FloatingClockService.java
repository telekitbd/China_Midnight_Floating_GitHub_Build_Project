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
    private TextView times;
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
                times.setText("BD " + bd + "  ·  CN " + cn +
                    "\n午夜 " + String.format(Locale.US,"%02d:%02d:%02d.%03d",h,m,s,ms));
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
        handler.post(tick);
    }

    private void makeBubble() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(9,5,6,5);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.argb(238,25,27,34));
        shape.setCornerRadius(18f);
        shape.setStroke(1, Color.rgb(65,75,91));
        box.setBackground(shape);

        times = new TextView(this);
        times.setTextColor(Color.WHITE);
        times.setTextSize(10);
        times.setTypeface(Typeface.MONOSPACE);
        times.setMaxLines(2);
        box.addView(times, new LinearLayout.LayoutParams(-2,-2));

        TextView close = new TextView(this);
        close.setText(" × ");
        close.setTextSize(17);
        close.setTextColor(Color.rgb(80,220,160));
        close.setPadding(3,0,0,0);
        box.addView(close);
        close.setOnClickListener(v -> stopSelf());

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

        box.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY; float downX, downY;
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    startX=p.x; startY=p.y; downX=e.getRawX(); downY=e.getRawY(); return true;
                } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    p.x=startX+(int)(e.getRawX()-downX);
                    p.y=startY+(int)(e.getRawY()-downY);
                    try { wm.updateViewLayout(box,p); } catch (Exception ignored) {}
                    return true;
                }
                return true;
            }
        });
        bubble = box;
        wm.addView(bubble,p);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() {
        handler.removeCallbacks(tick);
        if (wm != null && bubble != null) try { wm.removeView(bubble); } catch (Exception ignored) {}
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
