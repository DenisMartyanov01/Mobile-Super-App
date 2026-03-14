package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;

public class FullScreenImageActivity extends AppCompatActivity {

    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        fullScreenImageView = findViewById(R.id.fullScreenImageView);
        View btnClose = findViewById(R.id.btnClose);

        String imagePath = getIntent().getStringExtra("IMAGE_PATH");

        if (imagePath != null) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Glide.with(this)
                            .load(imageFile)
                            .into(fullScreenImageView);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fullScreenImageView.setOnClickListener(v -> finish());

        btnClose.setOnClickListener(v -> finish());
    }
}