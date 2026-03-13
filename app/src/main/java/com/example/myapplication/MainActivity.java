package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton btnCreateNote;
    private DatabaseHelper db;
    private DragNDropListView listViewNotes;
    private List<NoteData> notesList;
    private NoteAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        btnCreateNote = findViewById(R.id.btnCreateNote);
        listViewNotes = findViewById(R.id.listViewNotes);

        notesList = new ArrayList<>();
        adapter = new NoteAdapter(this, notesList);
        listViewNotes.setAdapter(adapter);

        setupDragAndDrop();

        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                startActivity(intent);
            }
        });

        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NoteData note = notesList.get(position);
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                intent.putExtra("NOTE_ID", note.getId());
                startActivity(intent);
            }
        });

        listViewNotes.setOnItemLongClickListener(null);

        loadNotes();
    }

    private void setupDragAndDrop() {
        listViewNotes.setDragEnabled(true);

        listViewNotes.setDropListener(new DragNDropListView.DropListener() {
            @Override
            public void onDrop(int from, int to) {
                if (from != to) {
                    saveNewOrder();
                    Toast.makeText(MainActivity.this, "Порядок изменен", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void saveNewOrder() {
        for (int i = 0; i < notesList.size(); i++) {
            notesList.get(i).setOrder(i);
        }
        db.updateNotesOrder(notesList);
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