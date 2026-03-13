package com.example.myapplication;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {

    private Context context;

    public ImageHelper(Context context) {
        this.context = context;
    }

    // Сохраняем изображение во внутреннее хранилище и возвращаем новый путь
    public String saveImageToInternalStorage(Uri imageUri) {
        try {
            // Создаем уникальное имя файла
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";

            // Получаем директорию для изображений
            File storageDir = context.getFilesDir();
            File imageFile = new File(storageDir, imageFileName);

            // Копируем содержимое URI в файл
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            // Возвращаем абсолютный путь к файлу
            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Удаляем изображение из внутреннего хранилища
    public boolean deleteImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            return imageFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получаем Bitmap из пути к файлу
    public Bitmap getImageBitmap(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Проверяем, существует ли файл
    public boolean imageExists(String imagePath) {
        if (imagePath == null) return false;
        File imageFile = new File(imagePath);
        return imageFile.exists();
    }
}