package io.github.kdetard.koki.feature.home;

import android.view.View;

import androidx.appcompat.widget.Toolbar;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerHomeBinding;
import io.github.kdetard.koki.feature.base.BaseController;

public class HomeController extends BaseController {
    ControllerHomeBinding binding;

    public HomeController() {
        super(R.layout.controller_home);
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
