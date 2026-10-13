package com.example.chinamidnight;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.rgb(16,19,27));
        getWindow().setNavigationBarColor(Color.rgb(16,19,27));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(24,24,24,24);
        root.setBackgroundColor(Color.rgb(16,19,27));
        setContentView(root);

        TextView title = new TextView(this);
        title.setText("CHINA MIDNIGHT");
        title.setTextSize(23); title.setTextColor(Color.rgb(73,220,157));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Bangladesh + China time · auto alarm at China 00:00");
        desc.setTextColor(Color.LTGRAY); desc.setTextSize(14);
        desc.setGravity(Gravity.CENTER); desc.setPadding(0,12,0,24);
        root.addView(desc);

        Button floatBtn = new Button(this);
        floatBtn.setText("START TINY FLOATING CLOCK");
        root.addView(floatBtn, new LinearLayout.LayoutParams(-1,-2));
        floatBtn.setOnClickListener(v -> startFloating());

        Button alarmBtn = new Button(this);
        alarmBtn.setText("SET CHINA MIDNIGHT VIBRATION");
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1,-2);
        ap.topMargin = 12; root.addView(alarmBtn, ap);
        alarmBtn.setOnClickListener(v -> scheduleMidnight());

        status = new TextView(this);
        status.setTextColor(Color.LTGRAY); status.setTextSize(13);
        status.setGravity(Gravity.CENTER); status.setPadding(0,18,0,0);
        root.addView(status);
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 7);
        }
    }

    private void startFloating() {
        if (!Settings.canDrawOverlays(this)) {
            status.setText("Allow 'Display over other apps', then tap Start again.");
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(i);
            return;
        }
        startService(new Intent(this, FloatingClockService.class));
        status.setText("Tiny floating clock started. Setting China midnight vibration...");
        scheduleMidnight();
    }

    private long nextChinaMidnight() {
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("Asia/Shanghai");
        java.util.Calendar c = java.util.Calendar.getInstance(tz);
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        if (c.getTimeInMillis() <= System.currentTimeMillis()) c.add(java.util.Calendar.DAY_OF_YEAR, 1);
        return c.getTimeInMillis();
    }

    private void scheduleMidnight() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            status.setText("Allow Alarms & reminders, then press the alarm button again.");
            startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:" + getPackageName())));
            return;
        }
        Intent intent = new Intent(this, MidnightReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 101, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextChinaMidnight(), pi);
        status.setText("China midnight vibration is set for 00:00.");
    }
}
