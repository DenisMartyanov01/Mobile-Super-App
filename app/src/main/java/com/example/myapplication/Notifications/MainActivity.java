package com.example.myapplication.Notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Notes.NotesActivity;
import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener,
        NotificationDialog.NotificationDialogListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private FloatingActionButton fabAdd;

    private List<NotificationData> notifications = new ArrayList<>();
    private int nextId = 1;

    private Handler updateHandler;
    private Runnable updateRunnable;
    private static final long CHECK_INTERVAL_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        fabAdd.setOnClickListener(v -> showNotificationDialog(null));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setAdapter(adapter);

        loadData();

        startInstantNotificationChecker();
    }

    public void GoToNotesActivity(View v)
    {
        Intent intent = new Intent(this, NotesActivity.class);
        startActivity(intent);
    }

    private void loadData()
    {
        notifications.clear();

//        Calendar calendar = Calendar.getInstance();
//
//        Intent intent = new Intent(this, SimpleReceiver.class);

//        for(int i = 0; i < 20; i++) {
//            PendingIntent pi = PendingIntent.getBroadcast(
//                    this,
//                    i,
//                    intent,
//                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
//            );
//
//            if (pi == null) continue;
//
//            calendar.add(Calendar.SECOND, 10);
//            notifications.add(new NotificationData(
//                    i,
//                    "Тест: удалится через 10 сек",
//                    "Это уведомление удалится сразу по истечении времени",
//                    calendar.getTimeInMillis()
//            ));
//            nextId = i + 1;
//
//        }

        // Типо можно проверить существует ли этот pendingIntent, но он не могет его
        // дату узнатть, так что надо сохранять инфу уведомлений отдельно походу.


        adapter.notifyDataSetChanged();

        RemoveExpiredNotifications();
    }
    private void showNotificationDialog(NotificationData notificationData) {
        NotificationDialog dialog = new NotificationDialog();

        if (notificationData != null) {
            dialog.setNotificationData(notificationData);
        }

        dialog.setNotificationDialogListener(this);

        dialog.show(getSupportFragmentManager(), "notification_dialog");
    }

    @SuppressLint("ScheduleExactAlarm")
    private void CreateNotification(NotificationData data)
    {
        try {
            // Добавляем в список
            data.setId(nextId++);
            notifications.add(0, data);
            adapter.notifyDataSetChanged();

            // 1. Получаем AlarmManager
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e("MyTag", "AlarmManager is null");
                return;
            }

            // 2. Создаем Intent для BroadcastReceiver
            Intent intent = new Intent(this, SimpleReceiver.class);
            intent.setAction("MY_TEST_ACTION");
            intent.putExtra("extra", data.getMessage());
            intent.putExtra("notification_id", data.getId());
            intent.putExtra("title", data.getTitle());
            intent.putExtra("time", System.currentTimeMillis());

            // ВАЖНОЕ ИСПРАВЛЕНИЕ: Правильные флаги для PendingIntent
            int flags;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Для Android 12+ используем FLAG_IMMUTABLE или FLAG_MUTABLE с FLAG_UPDATE_CURRENT
                flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            } else {
                flags = PendingIntent.FLAG_UPDATE_CURRENT;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    data.getId(),
                    intent,
                    flags
            );

            long triggerTime = data.getTimeInMillis();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

            // 6. Проверяем, что все установлено правильно
            // Для проверки используем тот же набор флагов
            boolean isSet = (PendingIntent.getBroadcast(this, data.getId(),
                    intent, flags | PendingIntent.FLAG_NO_CREATE) != null);

        } catch (SecurityException e) {

            // Для Android 12+ нужно специальное разрешение
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    Intent permissionIntent = new Intent(
                            android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    );
                    startActivity(permissionIntent);
                    android.widget.Toast.makeText(this,
                            "Разрешите точные уведомления в настройках",
                            android.widget.Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void EditNotification(NotificationData data)
    {
        for (int i = 0; i < notifications.size(); i++)
        {
            if (notifications.get(i).getId() == data.getId())
            {
                notifications.set(i, data);
                break;
            }
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(this, SimpleReceiver.class);
        intent.setAction("MY_TEST_ACTION");
        intent.putExtra("extra", data.getMessage());
        intent.putExtra("notification_id", data.getId());
        intent.putExtra("title", data.getTitle());
        intent.putExtra("time", System.currentTimeMillis());

        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Для Android 12+ используем FLAG_IMMUTABLE или FLAG_MUTABLE с FLAG_UPDATE_CURRENT
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                data.getId(),
                intent,
                flags
        );

        long triggerTime = data.getTimeInMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }

    private void DeleteNotification(int id)
    {
        for (int i = 0; i < notifications.size(); i++)
        {
            if (notifications.get(i).getId() == id)
            {
                notifications.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(this, SimpleReceiver.class);
        intent.setAction("MY_TEST_ACTION");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                id,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

    }

    @Override
    public void onNotificationSaved(NotificationData notification)
    {
        if (notification.getId() == -1)
        {
            CreateNotification(notification);
        }
        else
        {
            EditNotification(notification);
        }

        adapter.notifyDataSetChanged();
        RemoveExpiredNotifications();
    }

    @Override
    public void onNotificationDeleted(int notificationId)
    {
        new AlertDialog.Builder(this)
                .setTitle("Удаление уведомления")
                .setMessage("Вы уверены, что хотите удалить это уведомление?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    DeleteNotification(notificationId);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onNotificationClick(NotificationData notification)
    {
        showNotificationDialog(notification);
    }

    @Override
    public void onNotificationLongClick(NotificationData notification)
    {
        showNotificationDetails(notification);
    }

    private void showNotificationDetails(NotificationData notification)
    {
        new AlertDialog.Builder(this)
                .setTitle("Информация об уведомлении")
                .setMessage(
                        "Заголовок: " + notification.getTitle() + "\n\n" +
                                "Сообщение: " + notification.getMessage() + "\n\n" +
                                "Время: " + notification.getFormattedTime() + "\n" +
                                "Статус: " + (notification.isTimePassed() ? "Просрочено" : "Активно") + "\n" +
                                "ID: " + notification.getId()
                )
                .setPositiveButton("Редактировать", (dialog, which) -> {
                    showNotificationDialog(notification);
                })
                .setNeutralButton("Удалить", (dialog, which) -> {
                    onNotificationDeleted(notification.getId());
                })
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void startInstantNotificationChecker() {
        updateHandler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateVisibleItems();

                RemoveExpiredNotifications();

                updateHandler.postDelayed(this, CHECK_INTERVAL_MS);
            }
        };
        updateHandler.post(updateRunnable);
    }

    private void updateVisibleItems() {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
            if (holder instanceof NotificationAdapter.ViewHolder) {
                int position = holder.getAdapterPosition();
                if (position >= 0 && position < notifications.size()) {
                    NotificationData notification = notifications.get(position);
                    NotificationAdapter.ViewHolder viewHolder =
                            (NotificationAdapter.ViewHolder) holder;

                    viewHolder.statusTextView.setText(notification.getRemainingTime());

                    if (notification.isTimePassed()) {
                        viewHolder.statusTextView.setTextColor(
                                getResources().getColor(android.R.color.holo_red_dark)
                        );
                        viewHolder.statusTextView.setText("ПРОСРОЧЕНО - удаление...");
                    } else {
                        long remaining = notification.getTimeInMillis() - System.currentTimeMillis();
                        long seconds = remaining / 1000;

                        if (seconds < 60) {
                            viewHolder.statusTextView.setText("Через " + seconds + " сек");
                            viewHolder.statusTextView.setTextColor(
                                    getResources().getColor(android.R.color.holo_red_dark)
                            );
                        } else if (seconds < 300) {
                            long minutes = seconds / 60;
                            viewHolder.statusTextView.setText("Через " + minutes + " мин");
                            viewHolder.statusTextView.setTextColor(
                                    getResources().getColor(android.R.color.holo_orange_dark)
                            );
                        } else {
                            long minutes = seconds / 60;
                            viewHolder.statusTextView.setText("Через " + minutes + " мин");
                            viewHolder.statusTextView.setTextColor(
                                    getResources().getColor(android.R.color.holo_green_dark)
                            );
                        }
                    }
                }
            }
        }
    }

    private void RemoveExpiredNotifications()
    {
        long currentTime = System.currentTimeMillis();
        boolean removedAny = false;

        for (int i = notifications.size() - 1; i >= 0; i--) {
            NotificationData notification = notifications.get(i);

            if (currentTime >= notification.getTimeInMillis()) {

                notifications.remove(i);
                adapter.notifyItemRemoved(i);
                removedAny = true;
            }
        }

        if (removedAny) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (updateHandler != null)
        {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
}