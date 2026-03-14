package com.example.myapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
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
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.R;
import com.example.myapplication.ImageHelper;
import com.example.myapplication.ThemeManager;

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
    private ThemeManager themeManager;
    private String noteId;
    private List<String> imagePaths = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    if (selectedImage != null) {
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
        themeManager = new ThemeManager(this);

        if (themeManager.isDarkMode()) {
            setTheme(R.style.Theme_MyApplication_Dark);
        } else {
            setTheme(R.style.Theme_MyApplication);
        }

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
        LinearLayout imageItemLayout = new LinearLayout(this);
        imageItemLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                200, 250);
        itemParams.setMargins(8, 8, 8, 8);
        imageItemLayout.setLayoutParams(itemParams);

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                200, 200);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setTag(imagePath);

        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            Glide.with(this)
                    .load(imageFile)
                    .centerCrop()
                    .into(imageView);
        }

        imageView.setOnClickListener(v -> {
            String path = (String) v.getTag();
            openFullScreenImage(path);
        });

        Button btnRemove = new Button(this);
        btnRemove.setText("✕");
        btnRemove.setTextSize(12);
        btnRemove.setPadding(0, 0, 0, 0);

        btnRemove.setOnClickListener(v -> {
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
                NoteData note = new NoteData(title, content, date);
                note.setImagePaths(imagePaths);
                long id = db.addNote(note);
                if (id != -1) {
                    Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                }
            } else {
                NoteData note = db.getNote(Long.parseLong(noteId));
                if (note != null) {
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

                imagesContainer.removeAllViews();
                if (note.getImagePaths() != null) {
                    imagePaths = new ArrayList<>(note.getImagePaths());
                    for (String path : imagePaths) {
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
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_note);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView titleTextView = dialog.findViewById(R.id.dialogTitle);
        TextView messageTextView = dialog.findViewById(R.id.dialogMessage);

        titleTextView.setText("Удаление заметки");
        messageTextView.setText("Вы уверены, что хотите удалить эту заметку?");

        Button cancelButton = dialog.findViewById(R.id.dialogCancelButton);
        Button deleteButton = dialog.findViewById(R.id.dialogDeleteButton);

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
                try {
                    NoteData note = db.getNote(Long.parseLong(noteId));
                    if (note != null && note.getImagePaths() != null) {
                        for (String imagePath : note.getImagePaths()) {
                            imageHelper.deleteImage(imagePath);
                        }
                    }

                    db.deleteNote(Long.parseLong(noteId));
                    Toast.makeText(EditNoteActivity.this, "Заметка удалена", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(EditNoteActivity.this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_delete_note);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            TextView titleTextView = dialog.findViewById(R.id.dialogTitle);
            TextView messageTextView = dialog.findViewById(R.id.dialogMessage);
            Button cancelButton = dialog.findViewById(R.id.dialogCancelButton);
            Button deleteButton = dialog.findViewById(R.id.dialogDeleteButton);

            titleTextView.setText("Несохраненные изменения");
            messageTextView.setText("У вас есть несохраненные изменения. Выйти без сохранения?");
            deleteButton.setText("Выйти");

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
                    if (noteId == null) {
                        for (String imagePath : imagePaths) {
                            imageHelper.deleteImage(imagePath);
                        }
                    }
                    dialog.dismiss();
                    finish();
                }
            });

            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        String currentTitle = editTextTitle.getText().toString().trim();
        String currentContent = editTextContent.getText().toString().trim();

        if (noteId == null) {
            return !currentTitle.isEmpty() || !currentContent.isEmpty() || !imagePaths.isEmpty();
        } else {
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