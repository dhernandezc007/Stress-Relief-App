package com.zybooks.moodrelief;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import java.util.Calendar;

/**
 * Plays looping ambient nature sounds (rain, forest, ocean, birds, crickets) shared across the
 * Mood and Breathing screens, with gentle fades when starting, stopping or switching.
 *
 * <p>Audio files live in res/raw and are named after the sound key, e.g. res/raw/rain.ogg.
 * A sound whose file is missing is reported as unavailable instead of crashing.
 */
final class AmbientPlayer {

    static final String OFF = "off";
    static final String AUTO = "auto";
    static final String RAIN = "rain";
    static final String FOREST = "forest";
    static final String OCEAN = "ocean";
    static final String BIRDS = "birds";
    static final String CRICKETS = "crickets";

    private static final String PREFS = "settings";
    private static final String KEY_SOUND = "ambient_sound";
    private static final float MAX_VOLUME = 0.8f;
    private static final long FADE_IN_MS = 1500;
    private static final long FADE_OUT_MS = 600;

    private static MediaPlayer player;
    private static String playingSound;
    private static int startedScreens = 0;

    private AmbientPlayer() {
    }

    // ---------- User choice ----------

    /** The option the user picked: OFF, AUTO, or a specific sound. Defaults to AUTO. */
    static String getChoice(Context context) {
        return prefs(context).getString(KEY_SOUND, AUTO);
    }

    static void setChoice(Context context, String choice) {
        prefs(context).edit().putString(KEY_SOUND, choice).apply();
        apply(context);
    }

    /** Turns AUTO into a concrete sound based on the time of day. */
    static String resolve(String choice) {
        if (!AUTO.equals(choice)) return choice;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) return BIRDS;      // morning
        if (hour >= 19 || hour < 5) return CRICKETS;   // night
        return FOREST;                                 // daytime
    }

    /** True if an audio file for this sound has been added to res/raw. */
    static boolean isAvailable(Context context, String sound) {
        return rawId(context, sound) != 0;
    }

    // ---------- Lifecycle (call from screens that play sound) ----------

    static void onScreenStarted(Context context) {
        startedScreens++;
        apply(context);
    }

    /** Pauses when the last sound-playing screen goes to the background. */
    static void onScreenStopped() {
        startedScreens = Math.max(0, startedScreens - 1);
        if (startedScreens == 0 && player != null && player.isPlaying()) {
            player.pause();
        }
    }

    /** Fades out and releases everything (e.g. when returning to the home screen). */
    static void stop() {
        switchTo(null, null);
    }

    // ---------- Playback ----------

    private static void apply(Context context) {
        if (startedScreens == 0) return;

        String sound = resolve(getChoice(context));
        if (OFF.equals(sound) || !isAvailable(context, sound)) {
            switchTo(null, null);
        } else if (sound.equals(playingSound) && player != null) {
            if (!player.isPlaying()) {
                player.start();
            }
        } else {
            switchTo(context, sound);
        }
    }

    private static void switchTo(Context context, String sound) {
        MediaPlayer old = player;
        player = null;
        playingSound = null;
        if (old != null) {
            fade(old, MAX_VOLUME, 0f, FADE_OUT_MS, old::release);
        }
        if (context == null || sound == null) return;

        MediaPlayer next = MediaPlayer.create(context.getApplicationContext(), rawId(context, sound));
        if (next == null) return;
        next.setLooping(true);
        next.setVolume(0f, 0f);
        next.start();
        player = next;
        playingSound = sound;
        fade(next, 0f, MAX_VOLUME, FADE_IN_MS, null);
    }

    private static void fade(MediaPlayer mp, float from, float to, long durationMs, Runnable onEnd) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(durationMs);
        animator.addUpdateListener(a -> {
            float volume = (float) a.getAnimatedValue();
            try {
                mp.setVolume(volume, volume);
            } catch (IllegalStateException ignored) {
                // Player was released mid-fade
            }
        });
        if (onEnd != null) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onEnd.run();
                }
            });
        }
        animator.start();
    }

    // ---------- Helpers ----------

    /**
     * Looks up res/raw/&lt;sound&gt; by name so the app still builds while some sound files
     * haven't been added yet.
     */
    @SuppressLint("DiscouragedApi")
    private static int rawId(Context context, String sound) {
        return context.getResources().getIdentifier(sound, "raw", context.getPackageName());
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
