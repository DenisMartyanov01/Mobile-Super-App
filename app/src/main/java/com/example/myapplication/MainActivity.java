package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener,
        NotificationDialog.NotificationDialogListener {

    // UI элементы
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private FloatingActionButton fabAdd;

    // Данные
    private List<NotificationData> notifications = new ArrayList<>();
    private int nextId = 1; // Счетчик для ID

    // Handler для обновления времени
    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация UI
        initViews();

        // Настройка RecyclerView
        setupRecyclerView();

        // Загрузка тестовых данных
        loadSampleData();

        // Запуск обновления времени
        startTimeUpdater();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        // Обработчик для кнопки добавления
        fabAdd.setOnClickListener(v -> showNotificationDialog(null));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadSampleData() {
        // Очищаем список
        notifications.clear();

        // Создаем несколько тестовых уведомлений
        Calendar calendar = Calendar.getInstance();

        // Уведомление 1 (через 1 час)
        calendar.add(Calendar.HOUR, 1);
        notifications.add(new NotificationData(
                nextId++,
                "Встреча с командой",
                "Обсуждение нового проекта в конференц-зале",
                calendar.getTimeInMillis()
        ));

        // Уведомление 2 (через 2 дня)
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.add(Calendar.HOUR, 3);
        notifications.add(new NotificationData(
                nextId++,
                "День рождения друга",
                "Не забыть купить подарок и поздравить",
                calendar.getTimeInMillis()
        ));

        // Уведомление 3 (просроченное - вчера)
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        notifications.add(new NotificationData(
                nextId++,
                "Позвонить врачу",
                "Записаться на прием к терапевту",
                calendar.getTimeInMillis()
        ));

        // Уведомление 4 (через 30 минут)
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        notifications.add(new NotificationData(
                nextId++,
                "Обед с коллегой",
                "Встреча в кафе в 13:00",
                calendar.getTimeInMillis()
        ));

        // Уведомление 5 (через 5 минут - скоро)
        calendar.add(Calendar.MINUTE, -25);
        calendar.add(Calendar.SECOND, 30);
        notifications.add(new NotificationData(
                nextId++,
                "Скоро дедлайн",
                "Завершить отчет до конца дня",
                calendar.getTimeInMillis()
        ));

        // Обновляем адаптер
        adapter.notifyDataSetChanged();
    }

    private void showNotificationDialog(NotificationData notificationData) {
        NotificationDialog dialog = new NotificationDialog();

        // Передаем данные для редактирования
        if (notificationData != null) {
            dialog.setNotificationData(notificationData);
        }

        // Устанавливаем слушатель
        dialog.setNotificationDialogListener(this);

        // Показываем диалог
        dialog.show(getSupportFragmentManager(), "notification_dialog");
    }

    // Реализация методов интерфейса NotificationDialogListener
    @Override
    public void onNotificationSaved(NotificationData notification) {
        if (notification.getId() == -1) {
            // Новое уведомление
            notification.setId(nextId++);
            notifications.add(0, notification); // Добавляем в начало списка
            Toast.makeText(this, "Уведомление создано", Toast.LENGTH_SHORT).show();
        } else {
            // Обновление существующего
            for (int i = 0; i < notifications.size(); i++) {
                if (notifications.get(i).getId() == notification.getId()) {
                    notifications.set(i, notification);
                    Toast.makeText(this, "Уведомление обновлено", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }

        // Обновляем список
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNotificationDeleted(int notificationId) {
        // Подтверждение удаления
        new AlertDialog.Builder(this)
                .setTitle("Удаление уведомления")
                .setMessage("Вы уверены, что хотите удалить это уведомление?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    performDelete(notificationId);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void performDelete(int notificationId) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId() == notificationId) {
                notifications.remove(i);
                adapter.notifyItemRemoved(i);
                Toast.makeText(this, "Уведомление удалено", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    // Реализация методов интерфейса OnNotificationClickListener
    @Override
    public void onNotificationClick(NotificationData notification) {
        // Показываем диалог для редактирования
        showNotificationDialog(notification);
    }

    @Override
    public void onNotificationLongClick(NotificationData notification) {
        // Показываем подробную информацию об уведомлении
        showNotificationDetails(notification);
    }

    private void showNotificationDetails(NotificationData notification) {
        new AlertDialog.Builder(this)
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

    private void startTimeUpdater() {
        updateHandler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // Обновляем отображение времени (только видимые элементы)
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                    if (holder instanceof NotificationAdapter.ViewHolder) {
                        int position = holder.getAdapterPosition();
                        if (position >= 0 && position < notifications.size()) {
                            NotificationData notification = notifications.get(position);
                            NotificationAdapter.ViewHolder viewHolder =
                                    (NotificationAdapter.ViewHolder) holder;

                            // Обновляем статус
                            viewHolder.statusTextView.setText(notification.getRemainingTime());

                            // Меняем цвет статуса
                            if (notification.isTimePassed()) {
                                viewHolder.statusTextView.setTextColor(
                                        getResources().getColor(android.R.color.holo_red_dark)
                                );
                            } else {
                                viewHolder.statusTextView.setTextColor(
                                        getResources().getColor(android.R.color.holo_green_dark)
                                );
                            }
                        }
                    }
                }

                // Повторяем каждую секунду
                updateHandler.postDelayed(this, 1000);
            }
        };

        // Запускаем обновление
        updateHandler.post(updateRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Останавливаем обновление при уничтожении активности
        if (updateHandler != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
}