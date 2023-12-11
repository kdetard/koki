package io.github.kdetard.koki.feature.main;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.datastore.rxjava3.RxDataStore;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.google.android.material.navigation.NavigationBarView;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.databinding.ActivityMainBinding;
import io.github.kdetard.koki.feature.assets.AssetsController;
import io.github.kdetard.koki.feature.base.BaseActivity;
import io.github.kdetard.koki.feature.base.ExpandedAppBarLayout;
import io.github.kdetard.koki.feature.base.ActivityLayoutProvider;
import io.github.kdetard.koki.feature.home.HomeController;
import io.github.kdetard.koki.feature.monitoring.MonitoringController;
import io.github.kdetard.koki.feature.onboard.OnboardController;
import io.github.kdetard.koki.feature.onboard.OnboardEvent;
import io.github.kdetard.koki.feature.settings.SettingsController;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;

@AndroidEntryPoint
public class MainActivity extends BaseActivity implements ActivityLayoutProvider {
    @Inject
    Flowable<OnboardEvent> onboardEvent;

    @Inject
    RxDataStore<Settings> settings;

    ActivityMainBinding binding;

    Router router;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        router = Conductor.attachRouter(this, binding.controllerContainer, savedInstanceState)
                .setPopRootControllerMode(Router.PopRootControllerMode.NEVER)
                .setOnBackPressedDispatcherEnabled(true);

        binding.getRoot().getViewTreeObserver().addOnPreDrawListener(this::onPreDrawListener);

        getAppBarLayout().setVisibility(View.GONE);

        getNavBar().setOnItemSelectedListener(item -> {
            final var id = item.getItemId();

            Controller controller = null;

            final var previousRoot = router.getBackstack().stream().reduce((a, b) -> b).orElse(null);

            if (previousRoot == null || previousRoot.tag() == null) return false;

            if (!Integer.valueOf(previousRoot.tag()).equals(id)) {
                if (id == R.id.nav_home) {
                    controller = new HomeController();
                } else if (id == R.id.nav_assets) {
                    controller = new AssetsController();
                } else if (id == R.id.nav_monitoring) {
                    controller = new MonitoringController();
                } else if (id == R.id.nav_settings) {
                    controller = new SettingsController();
                }

                getAppBarLayout().setVisibility(controller instanceof SettingsController ? View.VISIBLE : View.GONE);
                if (!(router.getBackstack()
                        .stream().reduce((a, b) -> b).map(RouterTransaction::controller).orElse(null)
                        instanceof HomeController))
                    router.popCurrentController();

                if (controller != null)
                    pushController(controller, id);
            }

            return true;
        });

        router.addChangeListener(new ControllerChangeHandler.ControllerChangeListener() {
            @Override
            public void onChangeStarted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
                final var previousRoot = router.getBackstack().stream().reduce((a, b) -> b).orElse(null);
                if (previousRoot == null || previousRoot.tag() == null) return;
                getNavBar().setSelectedItemId(Integer.parseInt(previousRoot.tag()));
            }

            @Override
            public void onChangeCompleted(@Nullable Controller to, @Nullable Controller from, boolean isPush, @NonNull ViewGroup container, @NonNull ControllerChangeHandler handler) {
            }
        });

        onboardEvent
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(e -> {
                    switch (e) {
                        case LOGGED_IN -> {
                            if (router.hasRootController() && !(router.getBackstack()
                                    .stream().findFirst().map(RouterTransaction::controller)
                                    .orElse(null) instanceof OnboardController))
                                return;
                            getNavBar().setVisibility(View.VISIBLE);
                            setRoot(new HomeController(), R.id.nav_home);
                        }
                        case LOGGED_OUT -> {
                            getNavBar().setVisibility(View.GONE);
                            setRoot(new OnboardController(), R.id.nav_onboard);
                        }
                    }
                })
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe();
    }

    private boolean onPreDrawListener() {
        if (router == null) return false;
        if (router.hasRootController()) {
            binding.getRoot().getViewTreeObserver().removeOnDrawListener(this::onPreDrawListener);
            return true;
        }
        return false;
    }

    public View getRoot() { return binding.getRoot(); }

    public ExpandedAppBarLayout getAppBarLayout() { return binding.appBar; }

    public Toolbar getToolbar() { return binding.toolbar; }

    public NavigationBarView getNavBar() {
        return (NavigationBarView) binding.bottomNav;
    }

    private void pushController(@NonNull Controller controller, int id) {
        router.pushController(RouterTransaction.with(controller)
            .pushChangeHandler(new FadeChangeHandler())
            .popChangeHandler(new FadeChangeHandler())
            .tag(String.valueOf(id)));
    }

    private void setRoot(@NonNull Controller controller, int id) {
        router.setRoot(RouterTransaction.with(controller)
            .pushChangeHandler(new FadeChangeHandler())
            .popChangeHandler(new FadeChangeHandler())
            .tag(String.valueOf(id)));
    }
}
