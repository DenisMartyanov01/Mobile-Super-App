package com.example.myapplication.Notifications;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;

import java.util.Calendar;

public class NotificationDialog extends DialogFragment {

    private EditText titleEditText;
    private EditText messageEditText;
    private Button dateButton;
    private Button timeButton;
    private Button saveButton;
    private Button deleteButton;

    private Calendar selectedDateTime = Calendar.getInstance();
    private NotificationData notificationData;
    private NotificationDialogListener listener;

    public interface NotificationDialogListener {
        void onNotificationSaved(NotificationData notification);
        void onNotificationDeleted(int notificationId);
    }

    public void setNotificationDialogListener(NotificationDialogListener listener) {
        this.listener = listener;
    }

    public void setNotificationData(NotificationData notificationData) {
        this.notificationData = notificationData;
        if (notificationData != null) {
            selectedDateTime.setTimeInMillis(notificationData.getTimeInMillis());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dialog_notification, container, false);

        initViews(view);

        if (notificationData != null)
        {
            loadNotificationData();
        } else
        {
            selectedDateTime.add(Calendar.HOUR, 1);
        }

        updateDateTimeButtons();

        return view;
    }

    private void initViews(View view)
    {
        titleEditText = view.findViewById(R.id.titleEditText);
        messageEditText = view.findViewById(R.id.messageEditText);
        dateButton = view.findViewById(R.id.dateButton);
        timeButton = view.findViewById(R.id.timeButton);
        saveButton = view.findViewById(R.id.saveButton);
        deleteButton = view.findViewById(R.id.deleteButton);

        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> saveNotification());
        deleteButton.setOnClickListener(v -> deleteNotification());

        if (notificationData != null)
        {
            deleteButton.setVisibility(View.VISIBLE);
        }
        else
        {
            deleteButton.setVisibility(View.GONE);
        }
    }

    private void loadNotificationData()
    {
        if (notificationData != null)
        {
            titleEditText.setText(notificationData.getTitle());
            messageEditText.setText(notificationData.getMessage());
        }
    }

    private void showDatePicker()
    {
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                        updateDateTimeButtons();
                    }
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void showTimePicker()
    {
        TimePickerDialog timePicker = new TimePickerDialog(
                requireContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        updateDateTimeButtons();
                    }
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        );
        timePicker.show();
    }

    private void updateDateTimeButtons()
    {
        // Форматируем дату
        String dateText = String.format("%02d.%02d.%d",
                selectedDateTime.get(Calendar.DAY_OF_MONTH),
                selectedDateTime.get(Calendar.MONTH) + 1,
                selectedDateTime.get(Calendar.YEAR));

        // Форматируем время
        String timeText = String.format("%02d:%02d",
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE));

        dateButton.setText("Дата: " + dateText);
        timeButton.setText("Время: " + timeText);
    }

    private void saveNotification() {
        String title = titleEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();

        // Проверка на пустые поля
        if (title.isEmpty()) {
            titleEditText.setError("Введите заголовок");
            return;
        }

        if (message.isEmpty()) {
            messageEditText.setError("Введите сообщение");
            return;
        }

        // Создаем или обновляем объект
        NotificationData notification;
        if (notificationData != null) {
            // Обновляем существующий
            notificationData.setTitle(title);
            notificationData.setMessage(message);
            notificationData.setTimeInMillis(selectedDateTime.getTimeInMillis());
            notification = notificationData;
        } else {
            // Создаем новый (id будет установлен в MainActivity)
            notification = new NotificationData(
                    -1, // временный id
                    title,
                    message,
                    selectedDateTime.getTimeInMillis()
            );
        }

        // Вызываем callback
        if (listener != null) {
            listener.onNotificationSaved(notification);
        }

        // Закрываем диалог
        dismiss();
    }

    private void deleteNotification() {
        if (notificationData != null && listener != null) {
            listener.onNotificationDeleted(notificationData.getId());
        }
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (notificationData != null) {
            dialog.setTitle("Редактировать уведомление");
        } else {
            dialog.setTitle("Создать уведомление");
        }
        return dialog;
    }
}