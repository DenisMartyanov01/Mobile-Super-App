package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextContent;
    private LinearLayout imagesContainer;
    private Button btnAddImage, btnSave, btnEdit, btnDelete, btnCancelEdit;
    private TextView textViewTitle, textViewContent;
    private View viewModeLayout, editModeLayout;

    private DatabaseHelper db;
    private String noteId;
    private boolean isEditMode = false;
    private List<String> imagePaths = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        String imagePath = selectedImage.toString();
                        imagePaths.add(imagePath);
                        addImageToContainer(imagePath);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        db = new DatabaseHelper(this);
        noteId = getIntent().getStringExtra("NOTE_ID");

        initViews();
        setupMode();
        setupClickListeners();

        if (noteId != null) {
            loadNote();
        }
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        imagesContainer = findViewById(R.id.imagesContainer);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSave = findViewById(R.id.btnSave);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewContent = findViewById(R.id.textViewContent);
        viewModeLayout = findViewById(R.id.viewModeLayout);
        editModeLayout = findViewById(R.id.editModeLayout);
    }

    private void setupMode() {
        if (noteId == null) {
            isEditMode = true;
            showEditMode();
        } else {
            isEditMode = false;
            showViewMode();
        }
    }

    private void showViewMode() {
        viewModeLayout.setVisibility(View.VISIBLE);
        editModeLayout.setVisibility(View.GONE);
    }

    private void showEditMode() {
        viewModeLayout.setVisibility(View.GONE);
        editModeLayout.setVisibility(View.VISIBLE);

        if (noteId != null) {
            editTextTitle.setText(textViewTitle.getText());
            editTextContent.setText(textViewContent.getText());
        }
    }

    private void setupClickListeners() {
        btnAddImage.setOnClickListener(v -> openImagePicker());

        btnSave.setOnClickListener(v -> saveNote());

        btnEdit.setOnClickListener(v -> {
            isEditMode = true;
            showEditMode();
        });

        btnDelete.setOnClickListener(v -> showDeleteDialog());

        btnCancelEdit.setOnClickListener(v -> {
            if (noteId == null) {
                finish();
            } else {
                isEditMode = false;
                showViewMode();
                loadNote();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void addImageToContainer(String imagePath) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                200, 200);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this).load(imagePath).into(imageView);

        imageView.setOnLongClickListener(v -> {
            imagesContainer.removeView(imageView);
            imagePaths.remove(imagePath);
            Toast.makeText(this, "Изображение удалено", Toast.LENGTH_SHORT).show();
            return true;
        });

        imagesContainer.addView(imageView);
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Введите заголовок", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        NoteData note;
        if (noteId == null) {
            note = new NoteData(title, content, date);
            note.setImagePaths(imagePaths);
            long id = db.addNote(note);
            noteId = String.valueOf(id);
            Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
        } else {
            note = db.getNote(Long.parseLong(noteId));
            note.setTitle(title);
            note.setContent(content);
            note.setDate(date);
            note.setImagePaths(imagePaths);
            db.updateNote(note);
            Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
        }

        isEditMode = false;
        loadNote();
        showViewMode();
    }

    private void loadNote() {
        NoteData note = db.getNote(Long.parseLong(noteId));

        textViewTitle.setText(note.getTitle());
        textViewContent.setText(note.getContent());

        imagesContainer.removeAllViews();
        if (note.getImagePaths() != null) {
            imagePaths = new ArrayList<>(note.getImagePaths());
            for (String path : imagePaths) {
                addImageToContainer(path);
            }
        }
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление заметки")
                .setMessage("Вы уверены, что хотите удалить эту заметку?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    db.deleteNote(Long.parseLong(noteId));
                    Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}