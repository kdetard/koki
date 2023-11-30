package io.github.kdetard.koki.feature.base;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

public interface ToolbarProvider {
    AppBarLayout getAppBarLayout();
    Toolbar getToolbar();
}
