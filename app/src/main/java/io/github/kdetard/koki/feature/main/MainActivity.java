package io.github.kdetard.koki.feature.main;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.annotation.NonNull;
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

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.Settings;
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
                            setRoot(new OnboardController());
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

    private void setRoot(@NonNull Controller controller) {
        router.setRoot(RouterTransaction.with(controller)
            .pushChangeHandler(new FadeChangeHandler())
            .popChangeHandler(new FadeChangeHandler()));
    }
}
