package io.github.kdetard.koki.feature.settings;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.feature.base.BaseController;

public class SettingsController extends BaseController {
    public SettingsController() { super(R.layout.controller_settings); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }
}
