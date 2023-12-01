package io.github.kdetard.koki.feature.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;

import dev.chrisbanes.insetter.Insetter;

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

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            Insetter.builder()
                    .paddingTop(WindowInsetsCompat.Type.statusBars(), true)
                    .applyToView(changedView);
        }
    }
}
