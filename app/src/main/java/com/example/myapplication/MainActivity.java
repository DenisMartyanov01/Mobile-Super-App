package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.NoteAdapter;
import com.example.myapplication.NoteData;
import com.example.myapplication.EditNoteActivity;
import com.example.myapplication.ThemeManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton btnCreateNote;
    private Button btnThemeToggle;
    private Button btnToggleSortMode;
    private DatabaseHelper db;
    private ListView listViewNotes;
    private List<NoteData> notesList;
    private NoteAdapter adapter;
    private ThemeManager themeManager;
    private boolean sortModeEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        themeManager = new ThemeManager(this);

        if (themeManager.isDarkMode()) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        btnCreateNote = findViewById(R.id.btnCreateNote);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);
        btnToggleSortMode = findViewById(R.id.btnToggleSortMode);
        listViewNotes = findViewById(R.id.listViewNotes);

        updateThemeButton();

        notesList = new ArrayList<>();
        adapter = new NoteAdapter(this, notesList);
        adapter.setOnItemMoveListener(new NoteAdapter.OnItemMoveListener() {
            @Override
            public void onMoveUp(int position) {
                moveNoteUp(position);
            }

            @Override
            public void onMoveDown(int position) {
                moveNoteDown(position);
            }
        });
        listViewNotes.setAdapter(adapter);

        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                startActivity(intent);
            }
        });

        btnThemeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTheme();
            }
        });

        btnToggleSortMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSortMode();
            }
        });

        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!sortModeEnabled) {
                    NoteData note = notesList.get(position);
                    Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                    intent.putExtra("NOTE_ID", note.getId());
                    startActivity(intent);
                }
            }
        });

        // Долгое нажатие для удаления - теперь используем кастомный диалог
        listViewNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showCustomDeleteDialog(position);
                return true;
            }
        });

        loadNotes();
    }

    private void showCustomDeleteDialog(final int position) {
        final NoteData note = notesList.get(position);

        // Создаем кастомный диалог
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_note);

        // Устанавливаем прозрачный фон для диалога
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Настраиваем текст
        TextView titleTextView = dialog.findViewById(R.id.dialogTitle);
        TextView messageTextView = dialog.findViewById(R.id.dialogMessage);

        titleTextView.setText("Удаление заметки");
        messageTextView.setText("Вы уверены, что хотите удалить заметку \"" + note.getTitle() + "\"?");

        // Настраиваем кнопки
        Button cancelButton = dialog.findViewById(R.id.dialogCancelButton);
        Button deleteButton = dialog.findViewById(R.id.dialogDeleteButton);

        // Меняем цвет кнопки удаления в зависимости от темы
        if (themeManager.isDarkMode()) {
            deleteButton.setBackgroundTintList(getResources().getColorStateList(R.color.delete_button_color_dark));
        } else {
            deleteButton.setBackgroundTintList(getResources().getColorStateList(R.color.delete_button_color));
        }

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteNote(Long.parseLong(note.getId()));
                notesList.remove(position);
                adapter.notifyDataSetChanged();
                saveNewOrder();
                Toast.makeText(MainActivity.this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void moveNoteUp(int position) {
        if (position > 0) {
            NoteData temp = notesList.get(position);
            notesList.set(position, notesList.get(position - 1));
            notesList.set(position - 1, temp);

            adapter.notifyDataSetChanged();
            saveNewOrder();

            Toast.makeText(this, "Заметка перемещена вверх", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveNoteDown(int position) {
        if (position < notesList.size() - 1) {
            NoteData temp = notesList.get(position);
            notesList.set(position, notesList.get(position + 1));
            notesList.set(position + 1, temp);

            adapter.notifyDataSetChanged();
            saveNewOrder();

            Toast.makeText(this, "Заметка перемещена вниз", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleSortMode() {
        sortModeEnabled = !sortModeEnabled;
        adapter.setShowArrows(sortModeEnabled);

        if (sortModeEnabled) {
            btnToggleSortMode.setText("✓ Готово");
            Toast.makeText(this, "Режим сортировки включен", Toast.LENGTH_SHORT).show();
        } else {
            btnToggleSortMode.setText("↕ Сортировать");
            Toast.makeText(this, "Режим сортировки выключен", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleTheme() {
        boolean isDarkMode = themeManager.toggleTheme();
        String message = isDarkMode ? "Темная тема включена" : "Светлая тема включена";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        recreate();
    }

    private void updateThemeButton() {
        btnThemeToggle.setText(themeManager.getThemeName());
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