package io.github.kdetard.koki.feature.monitoring;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.kdetard.koki.R;

public enum TimeFrameOptions {
    HOUR(R.string.hour),
    DAY(R.string.day),
    WEEK(R.string.week),
    MONTH(R.string.month),
    YEAR(R.string.year);

    private final int resId;

    TimeFrameOptions(final int resId) {
        this.resId = resId;
    }

    @NonNull
    public String getText(final Context context) {
        return context.getString(resId);
    }
}
