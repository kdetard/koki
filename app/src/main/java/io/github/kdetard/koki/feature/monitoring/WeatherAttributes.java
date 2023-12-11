package io.github.kdetard.koki.feature.monitoring;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.kdetard.koki.R;

public enum WeatherAttributes {
    HUMIDITY(R.string.humidity_with_unit, "humidity"),
    RAIN(R.string.rain_with_unit, "rain"),
    TEMPERATURE(R.string.temperature_with_unit, "temperature"),
    WIND_SPEED(R.string.wind_speed_with_unit, "wind_speed");

    private final int autoCompletionResId;
    @NonNull
    private final String openRemoteString;

    WeatherAttributes(final int autoCompletionResId, final @NonNull String openRemoteString) {
        this.autoCompletionResId = autoCompletionResId;
        this.openRemoteString = openRemoteString;
    }

    @NonNull
    public String getText(final Context context) {
        return context.getString(autoCompletionResId);
    }
}
