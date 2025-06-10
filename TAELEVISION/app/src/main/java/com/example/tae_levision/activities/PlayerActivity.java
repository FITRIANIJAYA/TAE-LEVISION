package com.example.tae_levision.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tae_levision.R;

public class PlayerActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Get video ID from intent
        videoId = getIntent().getStringExtra("video_id");

        // Initialize back button
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // If videoId is available, open YouTube
        if (videoId != null && !videoId.isEmpty()) {
            openYouTubeVideo(videoId);
        } else {
            Toast.makeText(this, "Video ID tidak tersedia", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void openYouTubeVideo(String videoId) {
        // Create intent to open YouTube app or website
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));

        // Try to open in YouTube app first
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
            finish(); // Close this activity as we're redirecting
        } else {
            // If YouTube app is not installed, open in browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Tidak dapat membuka video YouTube", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}