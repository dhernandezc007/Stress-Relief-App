package com.zybooks.moodrelief;


import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Danny Hernandez
// Final Project with AI
// 11/24/2025
// Music Used: "Photograph" from movie soundtrack "Her"
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnAnxious).setOnClickListener(v -> openMood("anxious"));
        findViewById(R.id.btnUpset).setOnClickListener(v -> openMood("upset"));
        findViewById(R.id.btnDepressed).setOnClickListener(v -> openMood("depressed"));


        //  Find the boat image
        ImageView boat = findViewById(R.id.boatImage);

        // Load the animation
        Animation boatAnimation = AnimationUtils.loadAnimation(this, R.anim.boat_ride);

        // Start the animation
        boat.startAnimation(boatAnimation);


    }

    private void openMood(String mood) {
        Intent intent = new Intent(MainActivity.this, MoodActivity.class);
        intent.putExtra("mood", mood);
        startActivity(intent);
    }
}
