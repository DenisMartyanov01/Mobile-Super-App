package com.example.myapplication.Notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import com.example.myapplication.DatabaseHelper;

public class ViewNoteActivity extends AppCompatActivity {
    private TextView textViewTitle, textViewContent;
    private ImageView imageViewNote;
    private Button btnEdit, btnDelete;
    private DatabaseHelper db;
    private String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        db = new DatabaseHelper(this);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewContent = findViewById(R.id.textViewContent);
        imageViewNote = findViewById(R.id.imageViewNote);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        noteId = getIntent().getStringExtra("NOTE_ID");

        loadNote();

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNote();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote();
            }
        });
    }

    private void loadNote() {
        NoteData note = db.getNote(Long.parseLong(noteId));
        textViewTitle.setText(note.getTitle());
        textViewContent.setText(note.getContent());
        
        if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
            Glide.with(this)
                    .load(note.getImagePath())
                    .into(imageViewNote);
            imageViewNote.setVisibility(View.VISIBLE);
        } else {
            imageViewNote.setVisibility(View.GONE);
        }
    }

    private void editNote() {
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra("EDIT_NOTE_ID", noteId);
        startActivity(intent);
        finish();
    }

    private void deleteNote() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление заметки")
                .setMessage("Вы уверены, что хотите удалить эту заметку?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteNote(Long.parseLong(noteId));
                        Toast.makeText(ViewNoteActivity.this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}