package io.github.kdetard.koki.feature.home;

import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.feature.base.ToolbarProvider;
import io.github.kdetard.koki.databinding.ControllerHomeBinding;
import io.github.kdetard.koki.feature.base.BaseController;

public class HomeController extends BaseController implements ToolbarProvider {
    ControllerHomeBinding binding;

    public HomeController() {
        super(R.layout.controller_home);
    }

    public AppBarLayout getAppBarLayout() { return binding.appBar; }

    public Toolbar getToolbar() {
        return binding.toolbar;
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        binding = ControllerHomeBinding.bind(view);
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
        toolbar.inflateMenu(R.menu.home);
    }
}
