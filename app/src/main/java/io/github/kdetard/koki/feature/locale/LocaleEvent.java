package io.github.kdetard.koki.feature.locale;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.kdetard.koki.R;

public enum LocaleEvent {
    SYSTEM(R.string.language_system_default, R.string.language_system_default_short),
    EN_US(R.string.language_en_us, R.string.language_en_us_short),
    VI_VN(R.string.language_vi_vn, R.string.language_vi_vn_short)
    ;

    private final int stringResId;
    private final int localeResId;

    LocaleEvent(final int stringResId, final int localeResId) {
        this.stringResId = stringResId;
        this.localeResId = localeResId;
    }

    @NonNull
    public String getStringResId(Context context) { return context.getString(stringResId); }

    @NonNull
    public String getLocaleResId(Context context) { return context.getString(localeResId); }
}
