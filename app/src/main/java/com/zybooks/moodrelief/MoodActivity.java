package com.zybooks.moodrelief;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;


public class MoodActivity extends AppCompatActivity {

    private TextView quoteText;

    private String[] quotes;
    private int quoteIndex = -1;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mood);
        InsetsUtil.padForSystemBars(findViewById(R.id.moodContent));

        quoteText = findViewById(R.id.quoteText);

        // Get mood from Intent
        String mood = getIntent().getStringExtra("mood");
        bindMood(mood == null ? "" : mood);
        showNextQuote();

        // Ambient nature sounds (rain, forest, ocean, birds, crickets)
        SoundChips.bind(findViewById(R.id.soundChips), findViewById(R.id.soundHint));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNewQuote).setOnClickListener(v -> showNextQuote());
        findViewById(R.id.btnShareQuote).setOnClickListener(v -> shareQuote());
        findViewById(R.id.btnBreathe).setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), BreathingActivity.class)));
        findViewById(R.id.btnCall988).setOnClickListener(v ->
                openSafely(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:988"))));
        findViewById(R.id.btnText988).setOnClickListener(v ->
                openSafely(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:988"))));
    }

    /** Fills in the header, tip and quote list for the selected mood. */
    private void bindMood(String mood) {
        String emoji;
        int eyebrow, title, tipTitle, tipBody, quotesRes;

        switch (mood) {
            case "anxious":
                emoji = "🌊";
                eyebrow = R.string.mood_anxious_eyebrow;
                title = R.string.tile_anxious_sub;
                tipTitle = R.string.tip_anxious_title;
                tipBody = R.string.tip_anxious_body;
                quotesRes = R.array.quotes_anxious;
                break;
            case "upset":
                emoji = "🌧️";
                eyebrow = R.string.mood_upset_eyebrow;
                title = R.string.tile_upset_sub;
                tipTitle = R.string.tip_upset_title;
                tipBody = R.string.tip_upset_body;
                quotesRes = R.array.quotes_upset;
                break;
            case "depressed":
                emoji = "🌙";
                eyebrow = R.string.mood_depressed_eyebrow;
                title = R.string.tile_depressed_sub;
                tipTitle = R.string.tip_depressed_title;
                tipBody = R.string.tip_depressed_body;
                quotesRes = R.array.quotes_depressed;
                break;
            default:
                emoji = "🍃";
                eyebrow = R.string.mood_default_eyebrow;
                title = R.string.mood_default_title;
                tipTitle = R.string.tip_default_title;
                tipBody = R.string.tip_default_body;
                quotesRes = R.array.quotes_default;
                break;
        }

        ((TextView) findViewById(R.id.moodEmoji)).setText(emoji);
        ((TextView) findViewById(R.id.moodEyebrow)).setText(eyebrow);
        ((TextView) findViewById(R.id.moodTitle)).setText(title);
        ((TextView) findViewById(R.id.tipTitle)).setText(tipTitle);
        ((TextView) findViewById(R.id.tipBody)).setText(tipBody);
        quotes = getResources().getStringArray(quotesRes);
    }

    /** Picks a random quote, never repeating the one currently shown. */
    private void showNextQuote() {
        int next = random.nextInt(quotes.length);
        if (quotes.length > 1 && next == quoteIndex) {
            next = (next + 1) % quotes.length;
        }
        quoteIndex = next;

        quoteText.animate().alpha(0f).setDuration(150).withEndAction(() -> {
            quoteText.setText(quotes[quoteIndex]);
            quoteText.animate().alpha(1f).setDuration(250).start();
        }).start();
    }

    private void shareQuote() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, quotes[quoteIndex]);
        startActivity(Intent.createChooser(send, getString(R.string.quote_share)));
    }

    // ---------- Helpers ----------

    private void openSafely(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
            // No dialer / messaging app on this device (e.g. some tablets)
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AmbientPlayer.onScreenStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AmbientPlayer.onScreenStopped();
    }
}
