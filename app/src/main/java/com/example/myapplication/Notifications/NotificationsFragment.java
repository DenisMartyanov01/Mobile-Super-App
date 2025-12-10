package com.example.myapplication.Notifications;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener,
        NotificationDialog.NotificationDialogListener{

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private NotificationManager notificationManager;

    private List<NotificationData> notifications = new ArrayList<>();
    private int nextId = 1;
    private static final String CHANNEL_ID = "MY_NOTIFICATION_CHANNEL";
    private static final int NOTIFICATION_ID = 1;

    private Handler updateHandler;
    private Runnable updateRunnable;

    public NotificationsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        initViews(view);

        setupRecyclerView();

        loadSampleData();

        notificationManager = (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        startTimeUpdater();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);

//        fabAdd.setOnClickListener(v -> showNotificationDialog(null));
    }

    public void showNotification(View view) {
        Log.d("MyTag", "Показ уведомления начат");

        Intent intent = new Intent(requireContext(), requireActivity().getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Важно: проверяем версию Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Для Android 8.0+ используем канал
            Notification.Builder builder = new Notification.Builder(requireContext(), CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info) // временно системная иконка
                    .setContentTitle("SUBSCRIBE")
                    .setContentText("MOBILE APP DEVELOPMENT")
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setShowWhen(true);

            Notification notification = builder.build();
            notificationManager.notify(NOTIFICATION_ID, notification);
            Log.d("MyTag", "Уведомление создано (Android 8.0+)");

        } else {
            // Для старых версий Android
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext())
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("SUBSCRIBE")
                    .setContentText("MOBILE APP DEVELOPMENT")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            Notification notification = builder.build();
            notificationManager.notify(NOTIFICATION_ID, notification);
            Log.d("MyTag", "Уведомление создано (старая версия Android)");
        }

        // Проверяем разрешения для Android 13+
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {

                // Запрашиваем разрешение
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100);

                Log.d("MyTag", "Запрошено разрешение на уведомления");
            } else {
                Log.d("MyTag", "Разрешение на уведомления уже есть");
            }
        }
    }

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence channelName = "Мои уведомления";
            String channelDescription = "Канал для показа уведомлений";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Увеличиваем важность

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
    }

    private void setupRecyclerView()
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadSampleData()
    {
        notifications.clear();

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 1);
        notifications.add(new NotificationData(
                nextId++,
                "Встреча с командой",
                "Обсуждение нового проекта в конференц-зале",
                calendar.getTimeInMillis()
        ));

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.HOUR, 3);
        notifications.add(new NotificationData(
                nextId++,
                "День рождения друга",
                "Не забыть купить подарок и поздравить",
                calendar.getTimeInMillis()
        ));

        calendar.add(Calendar.DAY_OF_YEAR, -3);
        notifications.add(new NotificationData(
                nextId++,
                "Позвонить врачу",
                "Записаться на прием к терапевту",
                calendar.getTimeInMillis()
        ));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        notifications.add(new NotificationData(
                nextId++,
                "Обед с коллегой",
                "Встреча в кафе в 13:00",
                calendar.getTimeInMillis()
        ));

        calendar.add(Calendar.MINUTE, -25);
        calendar.add(Calendar.SECOND, 30);
        notifications.add(new NotificationData(
                nextId++,
                "Скоро дедлайн",
                "Завершить отчет до конца дня",
                calendar.getTimeInMillis()
        ));

        adapter.notifyDataSetChanged();
    }

    private void showNotificationDialog(NotificationData notificationData) {
        NotificationDialog dialog = new NotificationDialog();

        if (notificationData != null) {
            dialog.setNotificationData(notificationData);
        }

        dialog.setNotificationDialogListener(this);

        dialog.show(getChildFragmentManager(), "notification_dialog");
    }

    @Override
    public void onNotificationSaved(NotificationData notification)
    {
        if (notification.getId() == -1)
        {
            notification.setId(nextId++);
            notifications.add(0, notification); // Добавляем в начало списка
            Toast.makeText(getContext(), "Уведомление создано", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (int i = 0; i < notifications.size(); i++)
            {
                if (notifications.get(i).getId() == notification.getId())
                {
                    notifications.set(i, notification);
                    Toast.makeText(getContext(), "Уведомление обновлено", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNotificationDeleted(int notificationId)
    {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удаление уведомления")
                .setMessage("Вы уверены, что хотите удалить это уведомление?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    performDelete(notificationId);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void performDelete(int notificationId)
    {
        for (int i = 0; i < notifications.size(); i++)
        {
            if (notifications.get(i).getId() == notificationId)
            {
                notifications.remove(i);
                adapter.notifyItemRemoved(i);
                Toast.makeText(getContext(), "Уведомление удалено", Toast.LENGTH_SHORT).show();
                return;
            }
        }
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
        new AlertDialog.Builder(requireContext())
                .setTitle("Информация об уведомлении")
                .setMessage(
                        "Заголовок: " + notification.getTitle() + "\n\n" +
                                "Сообщение: " + notification.getMessage() + "\n\n" +
                                "Время: " + notification.getFormattedTime() + "\n" +
                                "Статус: " + (notification.isActive() ? "Активно" : "Неактивно") + "\n" +
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

    private void startTimeUpdater()
    {
        updateHandler = new Handler();
        updateRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < recyclerView.getChildCount(); i++)
                {
                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                    if (holder instanceof NotificationAdapter.ViewHolder)
                    {
                        int position = holder.getAdapterPosition();
                        if (position >= 0 && position < notifications.size())
                        {
                            NotificationData notification = notifications.get(position);
                            NotificationAdapter.ViewHolder viewHolder =
                                    (NotificationAdapter.ViewHolder) holder;

                            // Обновляем статус
                            viewHolder.statusTextView.setText(notification.getRemainingTime());

                            // Меняем цвет статуса
                            if (notification.isTimePassed())
                            {
                                viewHolder.statusTextView.setTextColor(
                                        requireContext().getColor(android.R.color.holo_red_dark)
                                );
                            }
                            else
                            {
                                viewHolder.statusTextView.setTextColor(
                                        requireContext().getColor(android.R.color.holo_green_dark)
                                );
                            }
                        }
                    }
                }
                updateHandler.postDelayed(this, 1000);
            }
        };
        updateHandler.post(updateRunnable);
    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (updateHandler != null)
        {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
}
