package com.example.myapplication.Notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.R;

import java.util.List;

public class NotesFragment extends Fragment {
    private Button btnCreateNote;
    private DatabaseHelper db;
    private ListView listViewNotes;
    private List<NoteData> notesList;
    private NoteAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        db = new DatabaseHelper(getActivity());

        btnCreateNote = view.findViewById(R.id.btnCreateNote);
        listViewNotes = view.findViewById(R.id.listViewNotes);

        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateNoteActivity.class);
                startActivity(intent);
            }
        });

        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NoteData note = notesList.get(position);
                Intent intent = new Intent(getActivity(), ViewNoteActivity.class);
                intent.putExtra("NOTE_ID", note.getId());
                startActivity(intent);
            }
        });

        loadNotes();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        notesList = db.getAllNotes();
        adapter = new NoteAdapter(getActivity(), notesList);
        listViewNotes.setAdapter(adapter);
    }
}
