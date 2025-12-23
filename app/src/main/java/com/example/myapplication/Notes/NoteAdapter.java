package com.example.myapplication.Notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;
import com.example.myapplication.R;

public class NoteAdapter extends ArrayAdapter<NoteData> {
    private Context context;
    private List<NoteData> notes;

    public NoteAdapter(Context context, List<NoteData> notes) {
        super(context, 0, notes);
        this.context = context;
        this.notes = notes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_note, parent, false);
        }

        NoteData note = notes.get(position);

        TextView titleTextView = convertView.findViewById(R.id.textViewNoteTitle);
        TextView previewTextView = convertView.findViewById(R.id.textViewNotePreview);
        ImageView imageThumbnail = convertView.findViewById(R.id.imageViewNoteThumbnail);

        titleTextView.setText(note.getTitle());

        String preview = note.getContent();
        if (preview.length() > 100) {
            preview = preview.substring(0, 100) + "...";
        }
        previewTextView.setText(preview);

        if (note.getImagePath() != null && !note.getImagePath().isEmpty()) {
            Glide.with(context)
                    .load(note.getImagePath())
                    .centerCrop()
                    .into(imageThumbnail);
            imageThumbnail.setVisibility(View.VISIBLE);
        } else {
            imageThumbnail.setVisibility(View.GONE);
        }

        return convertView;
    }
}