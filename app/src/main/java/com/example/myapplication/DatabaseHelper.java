package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 5;

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_IMAGES = "images";
    public static final String COLUMN_ORDER = "note_order";

    private static final String CREATE_TABLE_NOTES =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_IMAGES + " TEXT, " +
                    COLUMN_ORDER + " INTEGER DEFAULT 0)";

    private Gson gson = new Gson();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COLUMN_IMAGES + " TEXT");
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COLUMN_ORDER + " INTEGER DEFAULT 0");
            db.execSQL("UPDATE " + TABLE_NOTES + " SET " + COLUMN_ORDER + " = " + COLUMN_ID);
        }
    }

    public long addNote(NoteData note) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, note.getTitle());
            values.put(COLUMN_CONTENT, note.getContent());
            values.put(COLUMN_DATE, note.getDate());

            int maxOrder = getMaxOrder(db) + 1;
            values.put(COLUMN_ORDER, maxOrder);

            if (note.getImagePaths() != null && !note.getImagePaths().isEmpty()) {
                String imagesJson = gson.toJson(note.getImagePaths());
                values.put(COLUMN_IMAGES, imagesJson);
            }

            id = db.insert(TABLE_NOTES, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }

    private int getMaxOrder(SQLiteDatabase db) {
        int maxOrder = 0;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT MAX(" + COLUMN_ORDER + ") FROM " + TABLE_NOTES, null);
            if (cursor.moveToFirst()) {
                maxOrder = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return maxOrder;
    }

    public List<NoteData> getAllNotes() {
        List<NoteData> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_ORDER + " ASC";
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    NoteData note = new NoteData();
                    note.setId(cursor.getString(0));
                    note.setTitle(cursor.getString(1));
                    note.setContent(cursor.getString(2));
                    note.setDate(cursor.getString(3));

                    String imagesJson = cursor.getString(4);
                    if (imagesJson != null && !imagesJson.isEmpty()) {
                        Type type = new TypeToken<List<String>>(){}.getType();
                        List<String> images = gson.fromJson(imagesJson, type);
                        note.setImagePaths(images);
                    }

                    note.setOrder(cursor.getInt(5));

                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return notes;
    }

    public NoteData getNote(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        NoteData note = null;

        try {
            cursor = db.query(TABLE_NOTES,
                    new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_DATE, COLUMN_IMAGES, COLUMN_ORDER},
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                note = new NoteData(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3));
                note.setId(cursor.getString(0));

                String imagesJson = cursor.getString(4);
                if (imagesJson != null && !imagesJson.isEmpty()) {
                    Type type = new TypeToken<List<String>>(){}.getType();
                    List<String> images = gson.fromJson(imagesJson, type);
                    note.setImagePaths(images);
                }

                note.setOrder(cursor.getInt(5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return note;
    }

    public int updateNote(NoteData note) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, note.getTitle());
            values.put(COLUMN_CONTENT, note.getContent());
            values.put(COLUMN_DATE, note.getDate());

            if (note.getImagePaths() != null && !note.getImagePaths().isEmpty()) {
                String imagesJson = gson.toJson(note.getImagePaths());
                values.put(COLUMN_IMAGES, imagesJson);
            } else {
                values.put(COLUMN_IMAGES, (String) null);
            }

            result = db.update(TABLE_NOTES, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(note.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.delete(TABLE_NOTES, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateNotesOrder(List<NoteData> notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            for (int i = 0; i < notes.size(); i++) {
                NoteData note = notes.get(i);
                ContentValues values = new ContentValues();
                values.put(COLUMN_ORDER, i);
                db.update(TABLE_NOTES, values,
                        COLUMN_ID + " = ?",
                        new String[]{note.getId()});
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
}