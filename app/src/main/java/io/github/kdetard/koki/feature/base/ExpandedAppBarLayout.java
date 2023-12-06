package io.github.kdetard.koki.feature.base;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.appbar.AppBarLayout;

public class ExpandedAppBarLayout extends AppBarLayout {
    public ExpandedAppBarLayout(@NonNull Context context) {
        super(context);
    }

    public ExpandedAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
