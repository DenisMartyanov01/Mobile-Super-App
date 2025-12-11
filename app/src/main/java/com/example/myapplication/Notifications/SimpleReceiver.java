package com.example.myapplication.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class SimpleReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "MY_NOTIFICATION_CHANNEL";

    final String LOG_TAG = "myLogs";
    NotificationManager notificationManager;

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//        Log.d(LOG_TAG, "onReceive вызван");

        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("extra");
        int notificationId = intent.getIntExtra("notification_id", 0);

        if (title == null || title.isEmpty()) {
            title = intent.getAction();
            if (title == null) {
                title = "Напоминание";
            }
        }

        if (text == null || text.isEmpty()) {
            text = "Время пришло!";
        }

        if (notificationId == 0) {
            notificationId = (int) System.currentTimeMillis() % 10000;
        }

        // Вызываем метод для показа уведомления
        ShowNotification(context, title, text, notificationId);
    }

    private void ShowNotification(Context context, String Title, String Text, int notificationId)
    {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Важно: проверяем версию Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Для Android 8.0+ используем канал
            Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info) // временно системная иконка
                    .setContentTitle(Title)
                    .setContentText(Text)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true);

            Notification notification = builder.build();
            notificationManager.notify(NOTIFICATION_ID, notification);

        } else {
            // Для старых версий Android
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(Title)
                    .setContentText(Text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            Notification notification = builder.build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        checkNotificationPermission(context);
    }

    private void createNotificationChannel(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence channelName = "Мои уведомления";
            String channelDescription = "Канал для показа уведомлений";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    channelName,
                    importance
            );
            channel.setDescription(channelDescription);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});

            if (notificationManager != null)
            {
                notificationManager.createNotificationChannel(channel);
            }
        }

        checkNotificationPermission(context);
    }


    private void checkNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Открываем настройки уведомлений приложения
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(intent);
            } catch (Exception e) {
                // Альтернативный способ
                Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                appSettingsIntent.setData(uri);
                appSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(appSettingsIntent);
            }
        }
    }


}