package com.example.chinamidnight;

import android.Manifest;
import android.app.BroadcastOptions;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
                .setContentText("Midnight target reached.")
                .setAutoCancel(true).build());
        }
    }
}
