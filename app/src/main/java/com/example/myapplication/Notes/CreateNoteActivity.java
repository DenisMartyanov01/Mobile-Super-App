package com.example.myapplication.Notes;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

import com.example.myapplication.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextContent;
    private Button btnSave;
    private DatabaseHelper db;
    private boolean isEditing = false;
    private String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        db = new DatabaseHelper(this);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        btnSave = findViewById(R.id.btnSave);

        // Проверяем, редактируем ли существующую заметку
        if (getIntent().hasExtra("EDIT_NOTE_ID")) {
            isEditing = true;
            noteId = getIntent().getStringExtra("EDIT_NOTE_ID");
            loadNoteForEditing();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditing) {
                    updateNote();
                } else {
                    saveNote();
                }
            }
        });
    }

    private void loadNoteForEditing() {
        NoteData note = db.getNote(Long.parseLong(noteId));
        editTextTitle.setText(note.getTitle());
        editTextContent.setText(note.getContent());
        btnSave.setText("Обновить");
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Введите заголовок");
            return;
        }

        String currentDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        NoteData note = new NoteData(title, content, currentDate);
        long id = db.addNote(note);

        if (id != -1) {
            Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Введите заголовок");
            return;
        }

        String currentDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        NoteData note = new NoteData(title, content, currentDate);
        note.setId(noteId);

        int result = db.updateNote(note);

        if (result > 0) {
            Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
        }
    }
}