package com.zybooks.moodrelief;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Keeps content clear of the status bar, navigation bar, display cutouts and the keyboard. */
final class InsetsUtil {

    private InsetsUtil() {
    }

    /** Adds the system bar (or keyboard, when open) sizes on top of the view's existing XML padding. */
    static void padForSystemBars(View view) {
        final int left = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int right = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(bars.bottom, ime.bottom);
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bottomInset);
            return insets;
        });
    }
}
