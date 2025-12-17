package com.example.myapplication.Notes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import com.example.myapplication.DatabaseHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_CAMERA_CAPTURE = 2;
    private static final int REQUEST_PERMISSIONS = 3;
    
    private EditText editTextTitle, editTextContent;
    private Button btnSave, btnSelectImage, btnRemoveImage;
    private ImageView imageViewPreview;
    private DatabaseHelper db;
    private boolean isEditing = false;
    private String noteId;
    private String currentImagePath;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        db = new DatabaseHelper(this);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        btnSave = findViewById(R.id.btnSave);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        imageViewPreview = findViewById(R.id.imageViewPreview);

        // Проверяем, редактируем ли существующую заметку
        if (getIntent().hasExtra("EDIT_NOTE_ID")) {
            isEditing = true;
            noteId = getIntent().getStringExtra("EDIT_NOTE_ID");
            loadNoteForEditing();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditing) {
                    updateNote();
                } else {
                    saveNote();
                }
            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });

        btnRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeImage();
            }
        });
    }

    private void loadNoteForEditing() {
        NoteData note = db.getNote(Long.parseLong(noteId));
        editTextTitle.setText(note.getTitle());
        editTextContent.setText(note.getContent());
        btnSave.setText("Обновить");
        currentImagePath = note.getImagePath();
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            Glide.with(this)
                    .load(currentImagePath)
                    .into(imageViewPreview);
            imageViewPreview.setVisibility(View.VISIBLE);
            btnRemoveImage.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Введите заголовок");
            return;
        }

        String currentDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        NoteData note = new NoteData(title, content, currentDate);
        note.setImagePath(currentImagePath);
        long id = db.addNote(note);

        if (id != -1) {
            Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNote() {
        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Введите заголовок");
            return;
        }

        String currentDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        NoteData note = new NoteData(title, content, currentDate);
        note.setId(noteId);
        note.setImagePath(currentImagePath);

        int result = db.updateNote(note);

        if (result > 0) {
            Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImagePickerDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Выберите источник изображения");
        builder.setItems(new CharSequence[]{"Галерея", "Камера"}, (dialog, which) -> {
            if (which == 0) {
                pickImageFromGallery();
            } else {
                captureImageFromCamera();
            }
        });
        builder.show();
    }

    private void pickImageFromGallery() {
        if (checkPermissions()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        }
    }

    private void captureImageFromCamera() {
        if (checkPermissions()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(this, "Ошибка создания файла", Toast.LENGTH_SHORT).show();
                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this,
                            "com.example.myapplication.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_CAMERA_CAPTURE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentImagePath = image.getAbsolutePath();
        return image;
    }

    private boolean checkPermissions() {
        boolean needsPermission = false;
        java.util.List<String> permissionsNeeded = new java.util.ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
            needsPermission = true;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
                needsPermission = true;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                needsPermission = true;
            }
        }
        
        if (needsPermission) {
            ActivityCompat.requestPermissions(this, 
                    permissionsNeeded.toArray(new String[0]), 
                    REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение предоставлено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Разрешение необходимо для работы с изображениями", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImage = data.getData();
                currentImagePath = selectedImage.toString();
                Glide.with(this)
                        .load(selectedImage)
                        .into(imageViewPreview);
                imageViewPreview.setVisibility(View.VISIBLE);
                btnRemoveImage.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_CAMERA_CAPTURE) {
                if (currentImagePath != null) {
                    Glide.with(this)
                            .load(currentImagePath)
                            .into(imageViewPreview);
                    imageViewPreview.setVisibility(View.VISIBLE);
                    btnRemoveImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void removeImage() {
        currentImagePath = null;
        imageViewPreview.setVisibility(View.GONE);
        btnRemoveImage.setVisibility(View.GONE);
        imageViewPreview.setImageDrawable(null);
    }
}