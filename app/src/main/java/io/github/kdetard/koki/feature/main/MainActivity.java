package io.github.kdetard.koki.feature.main;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.datastore.rxjava3.RxDataStore;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.google.android.material.navigation.NavigationBarView;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.databinding.ActivityMainBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.feature.base.ExpandedAppBarLayout;
import io.github.kdetard.koki.feature.base.NavigationProvider;
import io.github.kdetard.koki.feature.home.HomeController;
import io.github.kdetard.koki.feature.onboard.OnboardController;
import io.github.kdetard.koki.feature.onboard.OnboardEvent;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import timber.log.Timber;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationProvider {
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

        binding.getRoot().getViewTreeObserver().addOnPreDrawListener(router::hasRootController);

        onboardEvent
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(e -> {
                    switch (e) {
                        case LOGGED_IN -> {
                            hideNavigation(false);
                            router.setRoot(RouterTransaction.with(new HomeController()));
                        }
                        case LOGGED_OUT -> {
                            hideNavigation(true);
                            router.setRoot(RouterTransaction.with(new OnboardController()));
                        }
                    }
                })
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe();
    }

    public View getRoot() { return binding.getRoot(); }

    public ExpandedAppBarLayout getAppBarLayout() { return binding.appBar; }

    public Toolbar getToolbar() { return binding.toolbar; }

    public NavigationBarView getNavBar() { return (NavigationBarView) binding.bottomNav; }

    private void hideNavigation(boolean predicate) {
        final int visibility = predicate ? View.GONE : View.VISIBLE;
        getAppBarLayout().setVisibility(visibility);
        getNavBar().setVisibility(visibility);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.getRoot().getViewTreeObserver().removeOnPreDrawListener(router::hasRootController);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final var controller = router.getBackstack().get(router.getBackstackSize() - 1).controller();
        try {
            ((BaseController) controller).onConfigurationChange(newConfig);
        } catch (NullPointerException e) {
            Timber.d("An error occured when calling onConfigurationChange for controller: %s", controller);
        }
    }
}
