package com.example.chinamidnight;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class MidnightReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        boolean active = context.getSharedPreferences("floating_state", Context.MODE_PRIVATE).getBoolean("active", false);
        if (!active) return;
        try {
            Vibrator v;
            if (Build.VERSION.SDK_INT >= 31)
                v = ((VibratorManager)context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)).getDefaultVibrator();
            else v = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= 26)
                v.vibrate(VibrationEffect.createWaveform(new long[]{0,500,250,500,250,900},-1));
            else v.vibrate(1800);
        } catch (Exception ignored) {}

        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26)
            nm.createNotificationChannel(new NotificationChannel("midnight","China midnight alert",NotificationManager.IMPORTANCE_HIGH));
        if (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context,"midnight") : new Notification.Builder(context);
            nm.notify(101,b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("China time is 12:00 AM")
                .setContentText("China midnight reached. Next daily alert will be scheduled.")
                .setAutoCancel(true).build());
        }

        // Schedule the next China midnight only while floating clock remains active.
        try {
            if (!context.getSharedPreferences("floating_state", Context.MODE_PRIVATE).getBoolean("active", false)) return;
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) return;
            java.util.Calendar c = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Shanghai"));
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(java.util.Calendar.HOUR_OF_DAY, 0);
            c.set(java.util.Calendar.MINUTE, 0);
            c.set(java.util.Calendar.SECOND, 0);
            c.set(java.util.Calendar.MILLISECOND, 0);
            if (c.getTimeInMillis() <= System.currentTimeMillis()) c.add(java.util.Calendar.DAY_OF_YEAR, 1);
            Intent next = new Intent(context, MidnightReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, 101, next,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (Build.VERSION.SDK_INT >= 23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            else am.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        } catch (Exception ignored) {}
    }
}
