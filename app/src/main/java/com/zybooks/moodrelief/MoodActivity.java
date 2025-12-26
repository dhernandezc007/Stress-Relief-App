package com.zybooks.moodrelief;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MoodActivity extends AppCompatActivity {

    private TextView quoteText;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        quoteText = findViewById(R.id.quoteText);

        // Get mood from Intent
        String mood = getIntent().getStringExtra("mood");

        // Pick a quote
        String quote = getQuoteForMood(mood);
        quoteText.setText(quote);

        // Start music
        mediaPlayer = MediaPlayer.create(this, R.raw.calm);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        Button btnBreathe = findViewById(R.id.btnBreathe);

        btnBreathe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the breathing activity
                Intent intent = new Intent(getApplicationContext(), BreathingActivity.class);
                startActivity(intent);
            }
        });



    }

    private String getQuoteForMood(String mood) {
        switch (mood) {
            case "anxious":
                return "Breathe. You are stronger than you think.";
            case "upset":
                return "This moment will pass, and you will rise again.";
            case "depressed":
                return "You matter. Your story is not over.";
            default:
                return "You are doing your best, and that is enough.";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
