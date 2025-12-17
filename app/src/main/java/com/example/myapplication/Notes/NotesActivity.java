package com.example.myapplication.Notes;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {
    private FloatingActionButton btnCreateNote;
    private DatabaseHelper db;
    private ListView listViewNotes;
    private List<NoteData> notesList;
    private NoteAdapter adapter;
    private BottomNavigationView bottomNavigationView;

    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        db = new DatabaseHelper(this);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        btnCreateNote = findViewById(R.id.btnCreateNote);
        listViewNotes = findViewById(R.id.listViewNotes);

        notesList = new ArrayList<>();

        adapter = new NoteAdapter(this, notesList);
        listViewNotes.setAdapter(adapter);

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

        setupBottomNavigation();

        loadNotes();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_nav_notifications) {
                Intent intent = new Intent(NotesActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        notesList.clear();
        notesList.addAll(db.getAllNotes());

        adapter.notifyDataSetChanged();
    }
}