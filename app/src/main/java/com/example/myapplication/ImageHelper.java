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

    public String saveImageToInternalStorage(Uri imageUri) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";

            File storageDir = context.getFilesDir();
            File imageFile = new File(storageDir, imageFileName);

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            return imageFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

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

    public boolean imageExists(String imagePath) {
        if (imagePath == null) return false;
        File imageFile = new File(imagePath);
        return imageFile.exists();
    }
}