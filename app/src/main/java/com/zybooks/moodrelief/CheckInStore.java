package com.zybooks.moodrelief;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Saves one mood check-in per day (mood 1-5 plus an optional note) on the device.
 */
public class CheckInStore {

    private static final String PREFS_NAME = "daily_check_ins";
    private static final String KEY_MOOD = "mood_";
    private static final String KEY_NOTE = "note_";

    private final SharedPreferences prefs;
    private final SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public CheckInStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Returns the mood (1-5) saved for that day, or 0 if there was no check-in. */
    public int getMood(Calendar day) {
        return prefs.getInt(KEY_MOOD + keyFor(day), 0);
    }

    public String getNote(Calendar day) {
        return prefs.getString(KEY_NOTE + keyFor(day), "");
    }

    public void save(Calendar day, int mood, String note) {
        String key = keyFor(day);
        prefs.edit()
                .putInt(KEY_MOOD + key, mood)
                .putString(KEY_NOTE + key, note == null ? "" : note.trim())
                .apply();
    }

    /**
     * Number of consecutive days with a check-in, ending today. If today has no
     * check-in yet the streak still counts back from yesterday, so it isn't lost
     * before the user has had a chance to check in.
     */
    public int currentStreak() {
        Calendar day = Calendar.getInstance();
        if (getMood(day) == 0) {
            day.add(Calendar.DAY_OF_YEAR, -1);
        }
        int streak = 0;
        while (getMood(day) > 0) {
            streak++;
            day.add(Calendar.DAY_OF_YEAR, -1);
        }
        return streak;
    }

    private String keyFor(Calendar day) {
        return keyFormat.format(day.getTime());
    }
}
