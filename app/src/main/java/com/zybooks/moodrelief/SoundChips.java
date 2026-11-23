package com.zybooks.moodrelief;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/** Fills a ChipGroup with the ambient sound options and keeps it in sync with AmbientPlayer. */
final class SoundChips {

    private static final String[] KEYS = {
            AmbientPlayer.OFF, AmbientPlayer.AUTO, AmbientPlayer.RAIN, AmbientPlayer.FOREST,
            AmbientPlayer.OCEAN, AmbientPlayer.BIRDS, AmbientPlayer.CRICKETS
    };
    private static final int[] LABELS = {
            R.string.sound_off, R.string.sound_auto, R.string.sound_rain, R.string.sound_forest,
            R.string.sound_ocean, R.string.sound_birds, R.string.sound_crickets
    };

    private SoundChips() {
    }

    /**
     * @param hint optional text view that describes what's playing, e.g. "Auto: Morning birds".
     */
    static void bind(ChipGroup group, @Nullable TextView hint) {
        Context context = group.getContext();
        String current = AmbientPlayer.getChoice(context);
        group.removeAllViews();

        for (int i = 0; i < KEYS.length; i++) {
            String key = KEYS[i];

            Chip chip = new Chip(context);
            chip.setId(View.generateViewId());
            chip.setTag(key);
            chip.setText(LABELS[i]);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColor(ContextCompat.getColorStateList(context, R.color.chip_bg));
            chip.setTextColor(ContextCompat.getColorStateList(context, R.color.chip_text));
            chip.setChipStrokeWidth(0f);
            chip.setEnsureMinTouchTargetSize(false);

            boolean isSound = !AmbientPlayer.OFF.equals(key) && !AmbientPlayer.AUTO.equals(key);
            if (isSound && !AmbientPlayer.isAvailable(context, key)) {
                chip.setEnabled(false);
                chip.setAlpha(0.4f);
            }

            group.addView(chip);
            if (key.equals(current)) {
                chip.setChecked(true);
            }
        }

        group.setOnCheckedStateChangeListener((g, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip checked = g.findViewById(checkedIds.get(0));
            AmbientPlayer.setChoice(context, (String) checked.getTag());
            updateHint(hint);
        });
        updateHint(hint);
    }

    private static void updateHint(@Nullable TextView hint) {
        if (hint == null) return;
        Context context = hint.getContext();
        String choice = AmbientPlayer.getChoice(context);
        String sound = AmbientPlayer.resolve(choice);

        if (AmbientPlayer.OFF.equals(sound)) {
            hint.setText(R.string.sound_hint_off);
        } else if (!AmbientPlayer.isAvailable(context, sound)) {
            hint.setText(context.getString(R.string.sound_hint_missing, sound));
        } else if (AmbientPlayer.AUTO.equals(choice)) {
            hint.setText(context.getString(R.string.sound_hint_auto, context.getString(nameOf(sound))));
        } else {
            hint.setText(context.getString(R.string.sound_hint_playing, context.getString(nameOf(sound))));
        }
    }

    private static int nameOf(String sound) {
        switch (sound) {
            case AmbientPlayer.RAIN: return R.string.sound_name_rain;
            case AmbientPlayer.FOREST: return R.string.sound_name_forest;
            case AmbientPlayer.OCEAN: return R.string.sound_name_ocean;
            case AmbientPlayer.BIRDS: return R.string.sound_name_birds;
            default: return R.string.sound_name_crickets;
        }
    }
}
