package io.github.kdetard.koki.feature.base;

import androidx.appcompat.widget.Toolbar;

public interface ToolbarProvider {
    ExpandedAppBarLayout getAppBarLayout();
    Toolbar getToolbar();
}
