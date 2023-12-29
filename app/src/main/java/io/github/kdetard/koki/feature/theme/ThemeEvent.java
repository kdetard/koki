package io.github.kdetard.koki.feature.theme;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.Theme;

public enum ThemeEvent {
    LIGHT(R.string.theme_light, Theme.LIGHT),
    DARK(R.string.theme_dark, Theme.DARK),
    AUTO(R.string.theme_auto, Theme.AUTO)
    ;

    private final int stringResId;
    private final Theme theme;

    ThemeEvent(final int stringResId, final Theme theme) {
        this.stringResId = stringResId;
        this.theme = theme;
    }

    @NonNull
    public String getText(final Context context) {
        return context.getString(stringResId);
    }

    @NonNull
    public Theme getTheme() { return theme; }
}
