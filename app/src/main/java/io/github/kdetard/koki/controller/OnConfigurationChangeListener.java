package io.github.kdetard.koki.controller;

import android.content.res.Configuration;

import androidx.annotation.NonNull;

public interface OnConfigurationChangeListener {
    void onConfigurationChange(@NonNull Configuration newConfig);
}
