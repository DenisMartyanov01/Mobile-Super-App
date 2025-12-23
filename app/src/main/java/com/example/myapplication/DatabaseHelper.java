package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.Notes.NoteData;
import com.example.myapplication.Notifications.NotificationData;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_IMAGE_PATH = "image_path";

    // Таблица уведомлений
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COLUMN_NOTIFICATION_ID = "_id";
    public static final String COLUMN_NOTIFICATION_TITLE = "title";
    public static final String COLUMN_NOTIFICATION_MESSAGE = "message";
    public static final String COLUMN_NOTIFICATION_TIME = "time_in_millis";

    private static final String CREATE_TABLE_NOTES =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_IMAGE_PATH + " TEXT)";

    private static final String CREATE_TABLE_NOTIFICATIONS =
            "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                    COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOTIFICATION_TITLE + " TEXT, " +
                    COLUMN_NOTIFICATION_MESSAGE + " TEXT, " +
                    COLUMN_NOTIFICATION_TIME + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTES);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COLUMN_IMAGE_PATH + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL(CREATE_TABLE_NOTIFICATIONS);
        }
    }

    // Добавление заметки
    public long addNote(NoteData note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_DATE, note.getDate());
        values.put(COLUMN_IMAGE_PATH, note.getImagePath());

        long id = db.insert(TABLE_NOTES, null, values);
        db.close();
        return id;
    }

    // Получение всех заметок
    public List<NoteData> getAllNotes() {
        List<NoteData> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                NoteData note = new NoteData();
                note.setId(cursor.getString(0));
                note.setTitle(cursor.getString(1));
                note.setContent(cursor.getString(2));
                note.setDate(cursor.getString(3));
                if (cursor.getColumnCount() > 4) {
                    note.setImagePath(cursor.getString(4));
                }
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notes;
    }

    // Получение заметки по ID
    public NoteData getNote(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_DATE, COLUMN_IMAGE_PATH},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        NoteData note = new NoteData(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3));
        note.setId(cursor.getString(0));
        if (cursor.getColumnCount() > 4) {
            note.setImagePath(cursor.getString(4));
        }

        cursor.close();
        db.close();
        return note;
    }

    // Обновление заметки
    public int updateNote(NoteData note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_DATE, note.getDate());
        values.put(COLUMN_IMAGE_PATH, note.getImagePath());

        return db.update(TABLE_NOTES, values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    // Удаление заметки
    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    // ========== Методы для работы с уведомлениями ==========

    // Добавление уведомления
    public long addNotification(NotificationData notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_TITLE, notification.getTitle());
        values.put(COLUMN_NOTIFICATION_MESSAGE, notification.getMessage());
        values.put(COLUMN_NOTIFICATION_TIME, notification.getTimeInMillis());

        long id = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
        return id;
    }

    // Получение всех уведомлений
    public List<NotificationData> getAllNotifications() {
        List<NotificationData> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NOTIFICATIONS + " ORDER BY " + COLUMN_NOTIFICATION_TIME + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                NotificationData notification = new NotificationData();
                notification.setId(cursor.getInt(0));
                notification.setTitle(cursor.getString(1));
                notification.setMessage(cursor.getString(2));
                notification.setTimeInMillis(cursor.getLong(3));
                notifications.add(notification);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notifications;
    }

    // Получение уведомления по ID
    public NotificationData getNotification(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTIFICATIONS,
                new String[]{COLUMN_NOTIFICATION_ID, COLUMN_NOTIFICATION_TITLE, 
                             COLUMN_NOTIFICATION_MESSAGE, COLUMN_NOTIFICATION_TIME},
                COLUMN_NOTIFICATION_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        NotificationData notification = null;
        if (cursor != null && cursor.moveToFirst()) {
            notification = new NotificationData();
            notification.setId(cursor.getInt(0));
            notification.setTitle(cursor.getString(1));
            notification.setMessage(cursor.getString(2));
            notification.setTimeInMillis(cursor.getLong(3));
            cursor.close();
        }
        db.close();
        return notification;
    }

    // Обновление уведомления
    public int updateNotification(NotificationData notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION_TITLE, notification.getTitle());
        values.put(COLUMN_NOTIFICATION_MESSAGE, notification.getMessage());
        values.put(COLUMN_NOTIFICATION_TIME, notification.getTimeInMillis());

        int result = db.update(TABLE_NOTIFICATIONS, values,
                COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(notification.getId())});
        db.close();
        return result;
    }

    // Удаление уведомления
    public void deleteNotification(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS, COLUMN_NOTIFICATION_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }
}
