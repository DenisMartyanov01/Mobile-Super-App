package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import com.example.myapplication.R;

public class NoteAdapter extends ArrayAdapter<NoteData> {
    private Context context;
    private List<NoteData> notes;
    private OnItemMoveListener moveListener;
    private boolean showArrows = false;

    public interface OnItemMoveListener {
        void onMoveUp(int position);
        void onMoveDown(int position);
    }

    public NoteAdapter(Context context, List<NoteData> notes) {
        super(context, 0, notes);
        this.context = context;
        this.notes = notes;
    }

    public void setOnItemMoveListener(OnItemMoveListener listener) {
        this.moveListener = listener;
    }

    public void setShowArrows(boolean show) {
        this.showArrows = show;
        notifyDataSetChanged();
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
        ImageButton btnMoveUp = convertView.findViewById(R.id.btnMoveUp);
        ImageButton btnMoveDown = convertView.findViewById(R.id.btnMoveDown);
        LinearLayout arrowContainer = convertView.findViewById(R.id.arrowContainer);

        titleTextView.setText(note.getTitle());

        String preview = note.getContent();
        if (preview.length() > 100) {
            preview = preview.substring(0, 100) + "...";
        }
        previewTextView.setText(preview);

        if (showArrows) {
            arrowContainer.setVisibility(View.VISIBLE);

            btnMoveUp.setEnabled(position > 0);
            btnMoveUp.setAlpha(position > 0 ? 1.0f : 0.3f);

            btnMoveDown.setEnabled(position < notes.size() - 1);
            btnMoveDown.setAlpha(position < notes.size() - 1 ? 1.0f : 0.3f);

            btnMoveUp.setOnClickListener(v -> {
                if (moveListener != null && position > 0) {
                    moveListener.onMoveUp(position);
                }
            });

            btnMoveDown.setOnClickListener(v -> {
                if (moveListener != null && position < notes.size() - 1) {
                    moveListener.onMoveDown(position);
                }
            });
        } else {
            arrowContainer.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void swapItems(int from, int to) {
        if (from >= 0 && from < notes.size() && to >= 0 && to < notes.size()) {
            NoteData temp = notes.get(from);
            notes.set(from, notes.get(to));
            notes.set(to, temp);
            notifyDataSetChanged();
        }
    }
}