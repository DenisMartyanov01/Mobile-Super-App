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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextContent;
    private LinearLayout imagesContainer;
    private Button btnAddImage, btnSave, btnDelete;

    private DatabaseHelper db;
    private ImageHelper imageHelper;
    private String noteId;
    private List<String> imagePaths = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
                        // Сохраняем изображение во внутреннее хранилище
                        String savedPath = imageHelper.saveImageToInternalStorage(selectedImage);
                        if (savedPath != null) {
                            imagePaths.add(savedPath);
                            addImageToContainer(savedPath);
                        } else {
                            Toast.makeText(this, "Ошибка при сохранении изображения", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        db = new DatabaseHelper(this);
        imageHelper = new ImageHelper(this);
        noteId = getIntent().getStringExtra("NOTE_ID");

        initViews();
        setupClickListeners();

        if (noteId != null) {
            loadNote();
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        imagesContainer = findViewById(R.id.imagesContainer);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupClickListeners() {
        btnAddImage.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveNote());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void addImageToContainer(String imagePath) {
        // Создаем контейнер для изображения и кнопки удаления
        LinearLayout imageItemLayout = new LinearLayout(this);
        imageItemLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                200, 250);
        itemParams.setMargins(8, 8, 8, 8);
        imageItemLayout.setLayoutParams(itemParams);

        // ImageView для изображения
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                200, 200);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setTag(imagePath);

        // Загружаем изображение из файла
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            Glide.with(this)
                    .load(imageFile)
                    .centerCrop()
                    .into(imageView);
        }

        // Обработчик нажатия на изображение для открытия на весь экран
        imageView.setOnClickListener(v -> {
            String path = (String) v.getTag();
            openFullScreenImage(path);
        });

        // Кнопка удаления изображения
        Button btnRemove = new Button(this);
        btnRemove.setText("✕");
        btnRemove.setTextSize(12);
        btnRemove.setPadding(0, 0, 0, 0);

        btnRemove.setOnClickListener(v -> {
            // Удаляем файл изображения
            imageHelper.deleteImage(imagePath);
            imagesContainer.removeView(imageItemLayout);
            imagePaths.remove(imagePath);
        });

        imageItemLayout.addView(imageView);
        imageItemLayout.addView(btnRemove);

        imagesContainer.addView(imageItemLayout);
    }

    private void openFullScreenImage(String imagePath) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("IMAGE_PATH", imagePath);
        startActivity(intent);
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

        try {
            if (noteId == null) {
                // Создание новой заметки
                NoteData note = new NoteData(title, content, date);
                note.setImagePaths(imagePaths);
                long id = db.addNote(note);
                if (id != -1) {
                    Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Обновление существующей заметки
                NoteData note = db.getNote(Long.parseLong(noteId));
                if (note != null) {
                    // Удаляем старые изображения, которых больше нет
                    List<String> oldImages = note.getImagePaths();
                    if (oldImages != null) {
                        for (String oldImage : oldImages) {
                            if (!imagePaths.contains(oldImage)) {
                                imageHelper.deleteImage(oldImage);
                            }
                        }
                    }

                    note.setTitle(title);
                    note.setContent(content);
                    note.setDate(date);
                    note.setImagePaths(imagePaths);
                    db.updateNote(note);
                    Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void loadNote() {
        try {
            NoteData note = db.getNote(Long.parseLong(noteId));

            if (note != null) {
                editTextTitle.setText(note.getTitle());
                editTextContent.setText(note.getContent());

                // Загружаем изображения
                imagesContainer.removeAllViews();
                if (note.getImagePaths() != null) {
                    imagePaths = new ArrayList<>(note.getImagePaths());
                    for (String path : imagePaths) {
                        // Проверяем, существует ли файл
                        if (imageHelper.imageExists(path)) {
                            addImageToContainer(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка загрузки заметки", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Удаление заметки")
                .setMessage("Вы уверены, что хотите удалить эту заметку?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    try {
                        // Удаляем все изображения заметки
                        NoteData note = db.getNote(Long.parseLong(noteId));
                        if (note != null && note.getImagePaths() != null) {
                            for (String imagePath : note.getImagePaths()) {
                                imageHelper.deleteImage(imagePath);
                            }
                        }

                        db.deleteNote(Long.parseLong(noteId));
                        Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                    .setTitle("Несохраненные изменения")
                    .setMessage("У вас есть несохраненные изменения. Выйти без сохранения?")
                    .setPositiveButton("Выйти", (dialog, which) -> {
                        // Если выходим без сохранения, удаляем новые изображения
                        if (noteId == null) {
                            for (String imagePath : imagePaths) {
                                imageHelper.deleteImage(imagePath);
                            }
                        }
                        super.onBackPressed();
                    })
                    .setNegativeButton("Остаться", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        String currentTitle = editTextTitle.getText().toString().trim();
        String currentContent = editTextContent.getText().toString().trim();

        if (noteId == null) {
            // Новая заметка
            return !currentTitle.isEmpty() || !currentContent.isEmpty() || !imagePaths.isEmpty();
        } else {
            // Существующая заметка
            try {
                NoteData original = db.getNote(Long.parseLong(noteId));
                if (original != null) {
                    return !currentTitle.equals(original.getTitle()) ||
                            !currentContent.equals(original.getContent()) ||
                            !imagePaths.equals(original.getImagePaths());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}