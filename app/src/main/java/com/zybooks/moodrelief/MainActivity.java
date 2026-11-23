package com.zybooks.moodrelief;


import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Danny Hernandez
// Final Project with AI
// 11/24/2025
// Ambient sounds: rain, forest, ocean, birds, crickets (see res/raw)
public class MainActivity extends AppCompatActivity {

    private static final String[] MOOD_EMOJI = {"😞", "😕", "😐", "🙂", "😄"};
    private static final int[] MOOD_COLORS = {
            R.color.mood_1, R.color.mood_2, R.color.mood_3, R.color.mood_4, R.color.mood_5
    };

    private CheckInStore store;
    private int selectedMood = 0;

    private View checkInForm;
    private View checkInDone;
    private LinearLayout moodScale;
    private TextInputLayout noteLayout;
    private TextInputEditText noteInput;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        applyWindowInsets();

        store = new CheckInStore(this);

        checkInForm = findViewById(R.id.checkInForm);
        checkInDone = findViewById(R.id.checkInDone);
        moodScale = findViewById(R.id.moodScale);
        noteLayout = findViewById(R.id.noteLayout);
        noteInput = findViewById(R.id.noteInput);
        btnSave = findViewById(R.id.btnSaveCheckIn);

        // Relief shortcuts
        findViewById(R.id.btnAnxious).setOnClickListener(v -> openMood("anxious"));
        findViewById(R.id.btnUpset).setOnClickListener(v -> openMood("upset"));
        findViewById(R.id.btnDepressed).setOnClickListener(v -> openMood("depressed"));
        findViewById(R.id.btnBreathe).setOnClickListener(v -> openBreathing());

        // Check-in actions
        buildMoodScale();
        btnSave.setOnClickListener(v -> saveCheckIn());
        findViewById(R.id.btnEditCheckIn).setOnClickListener(v -> showCheckInForm(true));
        findViewById(R.id.btnSuggestBreathe).setOnClickListener(v -> openBreathing());

        bindHeader();
        bindAffirmation();
        refreshCheckIn();

        // Boat bobbing on the wave
        ImageView boat = findViewById(R.id.boatImage);
        Animation boatAnimation = AnimationUtils.loadAnimation(this, R.anim.boat_ride);
        boat.startAnimation(boatAnimation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ambient sounds belong to the relief screens; fade them out back on home
        AmbientPlayer.stop();
        // The greeting and "today" may have changed while the app was in the background
        bindHeader();
    }

    // ---------- Header ----------

    private void bindHeader() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int greeting = hour < 12 ? R.string.greeting_morning
                : hour < 17 ? R.string.greeting_afternoon
                : R.string.greeting_evening;

        ((TextView) findViewById(R.id.greetingText)).setText(greeting);
        ((TextView) findViewById(R.id.dateText)).setText(
                new SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(now.getTime()));
    }

    private void bindStreak() {
        int streak = store.currentStreak();
        TextView streakText = findViewById(R.id.streakText);
        if (streak == 0) {
            streakText.setText(getString(R.string.streak_none));
        } else {
            streakText.setText(getResources().getQuantityString(R.plurals.streak_days, streak, streak));
        }
    }

    private void bindAffirmation() {
        String[] affirmations = getResources().getStringArray(R.array.affirmations);
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        ((TextView) findViewById(R.id.affirmationText)).setText(affirmations[dayOfYear % affirmations.length]);
    }

    // ---------- Daily check-in ----------

    private void buildMoodScale() {
        String[] labels = getResources().getStringArray(R.array.mood_labels);
        int muted = ContextCompat.getColor(this, R.color.still_muted);
        int gap = dp(4);

        for (int i = 0; i < MOOD_EMOJI.length; i++) {
            final int mood = i + 1;

            String label = capitalize(labels[i]);
            SpannableString text = new SpannableString(MOOD_EMOJI[i] + "\n" + label);
            int labelStart = MOOD_EMOJI[i].length() + 1;
            text.setSpan(new RelativeSizeSpan(1.9f), 0, labelStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ForegroundColorSpan(muted), labelStart, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            TextView option = new TextView(this);
            option.setTypeface(ResourcesCompat.getFont(this, R.font.dm_sans_medium));
            option.setText(text);
            option.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            option.setGravity(Gravity.CENTER);
            option.setLineSpacing(dp(2), 1f);
            option.setPadding(0, dp(12), 0, dp(10));
            option.setBackgroundResource(R.drawable.mood_option);
            option.setClickable(true);
            option.setFocusable(true);
            option.setContentDescription(label);
            option.setOnClickListener(v -> selectMood(mood));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMarginStart(i == 0 ? 0 : gap);
            params.setMarginEnd(i == MOOD_EMOJI.length - 1 ? 0 : gap);
            moodScale.addView(option, params);
        }
    }

    private void selectMood(int mood) {
        selectedMood = mood;
        for (int i = 0; i < moodScale.getChildCount(); i++) {
            moodScale.getChildAt(i).setSelected(i == mood - 1);
        }
        noteLayout.setVisibility(View.VISIBLE);
        btnSave.setEnabled(true);
    }

    private void saveCheckIn() {
        if (selectedMood == 0) return;
        String note = noteInput.getText() == null ? "" : noteInput.getText().toString();
        store.save(Calendar.getInstance(), selectedMood, note);
        noteInput.clearFocus();
        refreshCheckIn();
    }

    private void refreshCheckIn() {
        Calendar today = Calendar.getInstance();
        int mood = store.getMood(today);
        if (mood == 0) {
            showCheckInForm(false);
        } else {
            showCheckInDone(mood, store.getNote(today));
        }
        bindStreak();
        bindWeek();
    }

    /** @param editing true when re-opening today's check-in, so the previous answer is pre-filled. */
    private void showCheckInForm(boolean editing) {
        checkInDone.setVisibility(View.GONE);
        checkInForm.setVisibility(View.VISIBLE);

        if (editing) {
            Calendar today = Calendar.getInstance();
            noteInput.setText(store.getNote(today));
            selectMood(store.getMood(today));
        } else {
            selectedMood = 0;
            for (int i = 0; i < moodScale.getChildCount(); i++) {
                moodScale.getChildAt(i).setSelected(false);
            }
            noteInput.setText("");
            noteLayout.setVisibility(View.GONE);
            btnSave.setEnabled(false);
        }
    }

    private void showCheckInDone(int mood, String note) {
        checkInForm.setVisibility(View.GONE);
        checkInDone.setVisibility(View.VISIBLE);

        String[] labels = getResources().getStringArray(R.array.mood_labels);
        String[] responses = getResources().getStringArray(R.array.mood_responses);

        TextView emoji = findViewById(R.id.doneEmoji);
        emoji.setText(MOOD_EMOJI[mood - 1]);
        emoji.setBackground(circle(ContextCompat.getColor(this, MOOD_COLORS[mood - 1]), 0));

        ((TextView) findViewById(R.id.doneTitle)).setText(getString(R.string.checkin_done_title, labels[mood - 1]));
        ((TextView) findViewById(R.id.doneMessage)).setText(responses[mood - 1]);

        TextView noteView = findViewById(R.id.doneNote);
        if (TextUtils.isEmpty(note)) {
            noteView.setVisibility(View.GONE);
        } else {
            noteView.setText("“" + note + "”");
            noteView.setVisibility(View.VISIBLE);
        }

        // Gently suggest a breathing exercise on harder days
        findViewById(R.id.btnSuggestBreathe).setVisibility(mood <= 2 ? View.VISIBLE : View.GONE);
    }

    // ---------- Week overview ----------

    private void bindWeek() {
        LinearLayout weekStrip = findViewById(R.id.weekStrip);
        weekStrip.removeAllViews();

        SimpleDateFormat dayLetter = new SimpleDateFormat("EEEEE", Locale.getDefault());
        int ink = ContextCompat.getColor(this, R.color.still_ink);
        int muted = ContextCompat.getColor(this, R.color.still_muted);
        int ring = ContextCompat.getColor(this, R.color.still_ring);

        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            boolean isToday = i == 6;
            int mood = store.getMood(day);

            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView letter = new TextView(this);
            letter.setText(dayLetter.format(day.getTime()));
            letter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            letter.setTextColor(isToday ? ink : muted);
            letter.setTypeface(ResourcesCompat.getFont(this, isToday ? R.font.dm_sans_bold : R.font.dm_sans_medium));
            column.addView(letter);

            TextView dot = new TextView(this);
            dot.setGravity(Gravity.CENTER);
            dot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            if (mood > 0) {
                dot.setText(MOOD_EMOJI[mood - 1]);
                dot.setBackground(circle(ContextCompat.getColor(this, MOOD_COLORS[mood - 1]), 0));
            } else {
                dot.setBackground(circle(0, isToday ? ink : ring));
            }
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(36), dp(36));
            dotParams.topMargin = dp(8);
            column.addView(dot, dotParams);

            weekStrip.addView(column, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    // ---------- Navigation ----------

    private void openMood(String mood) {
        Intent intent = new Intent(MainActivity.this, MoodActivity.class);
        intent.putExtra("mood", mood);
        startActivity(intent);
    }

    private void openBreathing() {
        startActivity(new Intent(MainActivity.this, BreathingActivity.class));
    }

    // ---------- Helpers ----------

    /** Pads content below the status bar and extends the wave behind the navigation bar. */
    private void applyWindowInsets() {
        View content = findViewById(R.id.content);
        View navBarSpacer = findViewById(R.id.navBarSpacer);
        final int baseTop = content.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            content.setPadding(content.getPaddingLeft(), baseTop + bars.top, content.getPaddingRight(), content.getPaddingBottom());
            ViewGroup.LayoutParams lp = navBarSpacer.getLayoutParams();
            lp.height = bars.bottom;
            navBarSpacer.setLayoutParams(lp);
            return insets;
        });
    }

    private GradientDrawable circle(int fillColor, int strokeColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(fillColor);
        if (strokeColor != 0) {
            shape.setStroke(dp(1.5f), strokeColor);
        }
        return shape;
    }

    private int dp(float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
