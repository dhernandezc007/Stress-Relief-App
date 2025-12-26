package com.zybooks.moodrelief;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BreathingActivity extends AppCompatActivity {

    private TextView tvGuide;
    private View viewCircle;
    private ValueAnimator breathingAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing);

        tvGuide = findViewById(R.id.tvGuide);
        viewCircle = findViewById(R.id.viewCircle);
        Button btnDone = findViewById(R.id.btnDone);

        startBreathingAnimation();

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes this activity and goes back
            }
        });
    }

    private void startBreathingAnimation() {
        // Animate from scale 1.0 (normal) to 1.5 (larger)
        breathingAnimator = ValueAnimator.ofFloat(1.0f, 1.5f);
        breathingAnimator.setDuration(4000); // 4 seconds for one breath in
        breathingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathingAnimator.setRepeatMode(ValueAnimator.REVERSE); // Grow then shrink
        breathingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        breathingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                viewCircle.setScaleX(value);
                viewCircle.setScaleY(value);


                if (animation.getAnimatedFraction() < 0.5 && breathingAnimator.getRepeatMode() != ValueAnimator.RESTART) {
                }
            }
        });

        // Use a listener to change text exactly when the animation repeats/reverses
        breathingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                // If text was Inhale, switch to Exhale, and vice versa
                if (tvGuide.getText().toString().equals("Inhale...")) {
                    tvGuide.setText("Exhale...");
                } else {
                    tvGuide.setText("Inhale...");
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                tvGuide.setText("Inhale...");
            }
        });

        breathingAnimator.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (breathingAnimator != null) {
            breathingAnimator.cancel();
        }
    }
}
