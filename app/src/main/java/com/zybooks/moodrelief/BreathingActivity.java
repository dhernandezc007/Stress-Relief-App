package com.zybooks.moodrelief;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class BreathingActivity extends AppCompatActivity {

    private static final long SESSION_MS = 60_000;   // 1 minute reset
    private static final long BREATH_MS = 4_000;     // 4 seconds in, 4 seconds out

    private TextView tvGuide;
    private TextView tvTimer;
    private TextView tvSubtitle;
    private View viewCircle;
    private View viewHalo;
    private MaterialButton btnDone;

    private ValueAnimator breathingAnimator;
    private CountDownTimer sessionTimer;
    private long remainingMs = SESSION_MS;
    private boolean inhaling = true;
    private boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_breathing);
        InsetsUtil.padForSystemBars(findViewById(R.id.breathingRoot));

        tvGuide = findViewById(R.id.tvGuide);
        tvTimer = findViewById(R.id.tvTimer);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        viewCircle = findViewById(R.id.viewCircle);
        viewHalo = findViewById(R.id.viewHalo);
        btnDone = findViewById(R.id.btnDone);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> finish()); // Closes this activity and goes back

        // Ambient nature sounds, shared with the mood screen
        SoundChips.bind(findViewById(R.id.soundChips), null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AmbientPlayer.onScreenStarted(this);
        // Resume the session if the user left and came back before it finished
        if (!finished) {
            startBreathingAnimation();
            startSessionTimer();
        }
    }

    private void startBreathingAnimation() {
        inhaling = true;
        tvGuide.setText(R.string.breathing_in);

        // Animate from scale 1.0 (normal) to 1.45 (larger) and back
        breathingAnimator = ValueAnimator.ofFloat(1.0f, 1.45f);
        breathingAnimator.setDuration(BREATH_MS);
        breathingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathingAnimator.setRepeatMode(ValueAnimator.REVERSE); // Grow then shrink
        breathingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        breathingAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            viewCircle.setScaleX(value);
            viewCircle.setScaleY(value);
            // The halo trails slightly bigger and fades in as you breathe in
            float halo = 1f + (value - 1f) * 1.3f;
            viewHalo.setScaleX(halo);
            viewHalo.setScaleY(halo);
            viewHalo.setAlpha(0.35f + (value - 1f));
        });

        // Switch the guide text exactly when the animation reverses
        breathingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                inhaling = !inhaling;
                tvGuide.setText(inhaling ? R.string.breathing_in : R.string.breathing_out);
            }
        });

        breathingAnimator.start();
    }

    private void startSessionTimer() {
        updateTimerText(remainingMs);
        sessionTimer = new CountDownTimer(remainingMs, 250) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                updateTimerText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                finishSession();
            }
        }.start();
    }

    private void updateTimerText(long ms) {
        long totalSeconds = (ms + 999) / 1000; // round up so it shows 1:00 at the start
        tvTimer.setText(getString(R.string.breathing_time_left, totalSeconds / 60, totalSeconds % 60));
    }

    private void finishSession() {
        finished = true;
        if (breathingAnimator != null) {
            breathingAnimator.cancel();
        }

        // Settle the circles back to rest
        viewCircle.animate().scaleX(1.2f).scaleY(1.2f).setDuration(800).start();
        viewHalo.animate().scaleX(1.3f).scaleY(1.3f).alpha(0.6f).setDuration(800).start();

        tvGuide.setText(R.string.breathing_done);
        tvSubtitle.setText(R.string.breathing_done_subtitle);
        tvTimer.setVisibility(View.INVISIBLE);
        btnDone.setText(R.string.breathing_finish);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AmbientPlayer.onScreenStopped();
        if (breathingAnimator != null) {
            breathingAnimator.cancel();
        }
        if (sessionTimer != null) {
            sessionTimer.cancel();
        }
    }
}
