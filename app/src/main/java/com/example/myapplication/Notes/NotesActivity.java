package com.example.myapplication.Notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Notifications.MainActivity;
import com.example.myapplication.R;

import com.example.myapplication.DatabaseHelper;

import java.util.List;

public class NotesActivity extends AppCompatActivity {
    private Button btnCreateNote;
    private ListView listViewNotes;
    private DatabaseHelper db;
    private List<NoteData> notesList;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        db = new DatabaseHelper(this);

        btnCreateNote = findViewById(R.id.btnCreateNote);
        listViewNotes = findViewById(R.id.listViewNotes);

        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotesActivity.this, CreateNoteActivity.class);
                startActivity(intent);
            }
        });

        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NoteData note = notesList.get(position);
                Intent intent = new Intent(NotesActivity.this, ViewNoteActivity.class);
                intent.putExtra("NOTE_ID", note.getId());
                startActivity(intent);
            }
        });

        loadNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        notesList = db.getAllNotes();
        adapter = new NoteAdapter(this, notesList);
        listViewNotes.setAdapter(adapter);
    }

    public void GoToNotificationsActivity(View v)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}