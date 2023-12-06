package io.github.kdetard.koki.feature.base;

import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationBarView;

public interface NavigationProvider {
    View getRoot();
    ExpandedAppBarLayout getAppBarLayout();
    Toolbar getToolbar();
    NavigationBarView getNavBar();
}
