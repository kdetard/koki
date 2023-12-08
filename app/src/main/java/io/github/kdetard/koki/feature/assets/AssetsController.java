package io.github.kdetard.koki.feature.assets;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.io.InputStream;
import java.util.Objects;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerAssetsBinding;
import io.github.kdetard.koki.feature.base.BaseController;

public class AssetsController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface AssetsEntryPoint {
    }

    AssetsEntryPoint entryPoint;
    ControllerAssetsBinding binding;

    public AssetsController() { super(R.layout.controller_assets); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        binding = ControllerAssetsBinding.bind(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), AssetsEntryPoint.class);
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }
}
