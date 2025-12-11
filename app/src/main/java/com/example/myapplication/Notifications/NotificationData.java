package com.example.myapplication.Notifications;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationData implements Serializable {
    private int id;
    private String title;
    private String message;
    private long timeInMillis;

    public NotificationData(int id, String title, String message, long timeInMillis) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timeInMillis = timeInMillis;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimeInMillis() { return timeInMillis; }
    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    public String getRemainingTime() {
        long remaining = timeInMillis - System.currentTimeMillis();

        if (remaining <= 0) {
            return "ПРОСРОЧЕНО";
        }

        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return "Через " + hours + " ч " + (minutes % 60) + " мин";
        } else if (minutes > 0) {
            return "Через " + minutes + " мин";
        } else {
            return "Через " + seconds + " сек";
        }
    }

    public boolean isTimePassed() {
        return System.currentTimeMillis() >= timeInMillis;
    }

    @Override
    public String toString() {
        return title + " - " + getFormattedTime();
    }
}