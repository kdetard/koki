package io.github.kdetard.koki.feature.main;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.datastore.rxjava3.RxDataStore;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.google.android.material.navigation.NavigationBarView;
import com.jakewharton.rxbinding4.view.RxView;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.Theme;
import io.github.kdetard.koki.databinding.ActivityMainBinding;
import io.github.kdetard.koki.feature.base.BaseActivity;
import io.github.kdetard.koki.feature.base.ExpandedAppBarLayout;
import io.github.kdetard.koki.feature.base.ActivityLayoutProvider;
import io.github.kdetard.koki.feature.onboard.OnboardController;
import io.github.kdetard.koki.feature.onboard.OnboardEvent;
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

    public ActivityMainBinding getBinding() { return binding; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        settings.data()
                .map(Settings::getTheme)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::onThemeEventChanged)
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        router = Conductor.attachRouter(this, binding.controllerContainer, savedInstanceState)
                .setPopRootControllerMode(Router.PopRootControllerMode.NEVER)
                .setOnBackPressedDispatcherEnabled(true);

        RxView.preDraws(binding.getRoot(), this::onPreDrawListener)
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe();

        getAppBarLayout().setVisibility(View.GONE);

        onboardEvent
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(e -> {
                    switch (e) {
                        case LOGGED_IN -> {
                            if (router.hasRootController() && !(router.getBackstack()
                                    .stream().findFirst().map(RouterTransaction::controller)
                                    .orElse(null) instanceof OnboardController))
                                return;
                            // Check if no view has focus:
                            var view = getCurrentFocus();
                            if (view != null) {
                                var imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                            getNavBar().setVisibility(View.VISIBLE);
                            setRoot(new MainController());
                        }
                        case LOGGED_OUT -> {
                            getNavBar().setVisibility(View.GONE);
                            getAppBarLayout().setVisibility(View.GONE);
                            setRoot(new OnboardController());
                        }
                    }
                })
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe();
    }

    private void onThemeEventChanged(Theme event) {
        switch (event) {
            case LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            case DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            case AUTO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private boolean onPreDrawListener() {
        if (router == null) return false;
        return router.hasRootController();
    }

    public View getRoot() { return binding.getRoot(); }

    public ExpandedAppBarLayout getAppBarLayout() { return binding.appBar; }

    public Toolbar getToolbar() { return binding.toolbar; }

    public NavigationBarView getNavBar() {
        return (NavigationBarView) binding.bottomNav;
    }

    private void setRoot(@NonNull Controller controller) {
        router.setRoot(RouterTransaction.with(controller)
            .pushChangeHandler(new FadeChangeHandler())
            .popChangeHandler(new FadeChangeHandler()));
    }
}
