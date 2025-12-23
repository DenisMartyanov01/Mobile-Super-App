package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Notes.NotesActivity;
import com.example.myapplication.Notifications.NotificationAdapter;
import com.example.myapplication.Notifications.NotificationData;
import com.example.myapplication.Notifications.NotificationDialog;
import com.example.myapplication.Notifications.SimpleReceiver;
import com.example.myapplication.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Date;
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
    private BottomNavigationView bottomNavigationView;
    private DatabaseHelper db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAdd = findViewById(R.id.fabAdd);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        fabAdd.setOnClickListener(v -> showNotificationDialog(null));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setAdapter(adapter);

        db = new DatabaseHelper(this);

        setupBottomNavigation();

        loadNotifications();

        startInstantNotificationChecker();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_nav_notes) {
                Intent intent = new Intent(MainActivity.this, NotesActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
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
    private void CreateNotification(NotificationData data) {
        try {
            // Сохраняем в БД и получаем ID
            long dbId = db.addNotification(data);
            data.setId((int) dbId);
            
            // Обновляем nextId если нужно
            if (dbId >= nextId) {
                nextId = (int) dbId + 1;
            }
            
            // Добавляем в список
            notifications.add(0, data);
            adapter.notifyDataSetChanged();

            // 1. Получаем AlarmManager
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e("MyTag", "AlarmManager is null");
                return;
            }

            // 2. Проверяем разрешение для Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Запрашиваем разрешение
                    Intent permissionIntent = new Intent(
                            android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    );
                    startActivity(permissionIntent);
                    Toast.makeText(this,
                            "Разрешите точные уведомления в настройках",
                            Toast.LENGTH_LONG).show();
                    return; // Не продолжаем без разрешения
                }
            }

            // 3. Создаем Intent для BroadcastReceiver
            Intent intent = new Intent(this, SimpleReceiver.class);
            intent.setAction("MY_TEST_ACTION");
            intent.putExtra("extra", data.getMessage());
            intent.putExtra("notification_id", data.getId());
            intent.putExtra("title", data.getTitle());
            intent.putExtra("time", System.currentTimeMillis());

            // 4. Создаем PendingIntent
            int flags;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

            // 5. Устанавливаем будильник
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

            if (!checkAllNotificationPermissions(this))
            {
                openNotificationSettings(this);
            }

            Log.d("MyTag", "Уведомление запланировано на: " + new Date(triggerTime));

        } catch (SecurityException e) {
            Log.e("MyTag", "SecurityException: " + e.getMessage());
            Toast.makeText(this,
                    "Ошибка безопасности. Проверьте разрешения",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void EditNotification(NotificationData data)
    {
        // Обновляем в БД
        db.updateNotification(data);
        
        // Обновляем в списке
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
        // Удаляем из БД
        db.deleteNotification(id);
        
        // Удаляем из списка
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

    public static boolean checkAllNotificationPermissions(Context context, int sdkVersion) {
        if (sdkVersion >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.areNotificationsEnabled()) {
                return false;
            }
        }

        if (sdkVersion >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                return false;
            }
        }

        return true;
    }

    // Оригинальный метод (для продакшена)
    public static boolean checkAllNotificationPermissions(Context context) {
        return checkAllNotificationPermissions(context, Build.VERSION.SDK_INT);
    }

    private void openNotificationSettings(Context context) {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // Фолбэк на общие настройки приложения
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
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
    public void onNotificationClick(NotificationData notification) {

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
                // Удаляем из БД
                db.deleteNotification(notification.getId());
                
                notifications.remove(i);
                adapter.notifyItemRemoved(i);
                removedAny = true;
            }
        }

        if (removedAny) {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadNotifications() {
        notifications.clear();
        List<NotificationData> dbNotifications = db.getAllNotifications();
        
        // Определяем максимальный ID для nextId
        int maxId = 0;
        for (NotificationData notification : dbNotifications) {
            notifications.add(notification);
            if (notification.getId() > maxId) {
                maxId = notification.getId();
            }
            // Восстанавливаем будильники для активных уведомлений
            if (!notification.isTimePassed()) {
                restoreAlarmForNotification(notification);
            }
        }
        nextId = maxId + 1;
        
        // Сортируем по времени (ближайшие сверху)
        notifications.sort((n1, n2) -> Long.compare(n1.getTimeInMillis(), n2.getTimeInMillis()));
        
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void restoreAlarmForNotification(NotificationData data) {
        try {
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
        } catch (SecurityException e) {
            Log.e("MyTag", "SecurityException при восстановлении будильника: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем список уведомлений (без повторного восстановления будильников)
        notifications.clear();
        List<NotificationData> dbNotifications = db.getAllNotifications();
        
        int maxId = 0;
        for (NotificationData notification : dbNotifications) {
            notifications.add(notification);
            if (notification.getId() > maxId) {
                maxId = notification.getId();
            }
        }
        nextId = maxId + 1;
        
        notifications.sort((n1, n2) -> Long.compare(n1.getTimeInMillis(), n2.getTimeInMillis()));
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (updateHandler != null)
        {
            updateHandler.removeCallbacks(updateRunnable);
        }
        if (db != null) {
            db.close();
        }
    }

}