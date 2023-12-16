package io.github.kdetard.koki.feature.main;

import android.view.View;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.viewpager2.RouterStateAdapter;

import java.util.Objects;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerMainBinding;
import io.github.kdetard.koki.feature.assets.AssetsController;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.feature.home.HomeController;
import io.github.kdetard.koki.feature.monitoring.MonitoringController;
import io.github.kdetard.koki.feature.settings.SettingsController;

public class MainController extends BaseController {
    ControllerMainBinding binding;
    RouterStateAdapter pagerAdapter;

    public MainController() {
        super(R.layout.controller_main);

        this.pagerAdapter = new RouterStateAdapter(this) {
            @Override
            public void configureRouter(@NonNull Router router, int i) {
                if (!router.hasRootController()) {
                    var page = switch (i) {
                        case 0 -> new HomeController();
                        case 1 -> new AssetsController();
                        case 2 -> new MonitoringController();
                        case 3 -> new SettingsController();
                        default -> throw new IllegalStateException("Unexpected value: " + i);
                    };

                    router.setRoot(RouterTransaction.with(page)
                            .pushChangeHandler(new FadeChangeHandler())
                            .popChangeHandler(new FadeChangeHandler()));
                }
            }

            @Override
            public int getItemCount() { return 4; }
        };
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        binding = ControllerMainBinding.bind(view);

        binding.getRoot().setUserInputEnabled(false);

        binding.getRoot().setAdapter(pagerAdapter);

        binding.getRoot().setCurrentItem(0, false);

        getNavBar().setOnItemSelectedListener(item -> {
            var order = switch (Objects.requireNonNull(item.getTitle()).toString()) {
                case "Home" -> 0;
                case "Assets" -> 1;
                case "Monitor" -> 2;
                case "Settings" -> 3;
                default -> throw new IllegalStateException("Unexpected value: " + item.getTitle());
            };

            getAppBarLayout().setVisibility(order == 3 ? View.VISIBLE : View.GONE);

            binding.getRoot().setCurrentItem(order, false);

            return true;
        });
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        if (!Objects.requireNonNull(getActivity()).isChangingConfigurations()) {
            binding.getRoot().setAdapter(null);
        }
        super.onDestroyView(view);
    }
}
