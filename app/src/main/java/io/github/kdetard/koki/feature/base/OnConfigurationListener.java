package io.github.kdetard.koki.feature.base;

import android.content.res.Configuration;

import androidx.annotation.NonNull;

public interface OnConfigurationListener {
    void onConfigurationChange(@NonNull Configuration newConfig);
}
