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
import android.widget.Toast;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.databinding.ActivityMainBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.feature.base.ExpandedAppBarLayout;
import io.github.kdetard.koki.feature.base.ToolbarProvider;
import io.github.kdetard.koki.feature.home.HomeController;
import io.github.kdetard.koki.feature.onboard.OnboardController;
import io.github.kdetard.koki.feature.onboard.OnboardEvent;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;
import timber.log.Timber;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements ToolbarProvider {
    @Inject
    PublishSubject<OnboardEvent> onboardEvent;

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

        if (router.hasRootController()) return;

        onboardEvent
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(e -> {
                    switch (e) {
                        case LOGGED_IN:
                            getAppBarLayout().setVisibility(View.VISIBLE);
                            router.setRoot(RouterTransaction.with(new HomeController()));
                            break;
                        case LOGGED_OUT:
                            getAppBarLayout().setVisibility(View.GONE);
                            router.setRoot(RouterTransaction.with(new OnboardController()));
                            break;
                    }
                })
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                .subscribe();

        settings.data()
                 .doOnNext(currentSettings -> {
                     final boolean isLoggedOut = currentSettings.getLoggedOut();
                     final boolean accessTokenIsEmpty = currentSettings.getAccessToken().isEmpty();
                     if (isLoggedOut || accessTokenIsEmpty) {
                         if (isLoggedOut && accessTokenIsEmpty) {
                             runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You are required to relogin", Toast.LENGTH_SHORT).show());
                         }
                         settings.updateDataAsync(newSettings ->
                                 Single.just(newSettings.toBuilder().setLoggedOut(true).setAccessToken("").build()));
                         onboardEvent.onNext(OnboardEvent.LOGGED_OUT);
                     } else {
                         onboardEvent.onNext(OnboardEvent.LOGGED_IN);
                     }
                 })
                 .to(autoDisposable(AndroidLifecycleScopeProvider.from(getLifecycle())))
                 .subscribe();
    }

    public ExpandedAppBarLayout getAppBarLayout() { return binding.appBar; }

    public Toolbar getToolbar() { return binding.toolbar; }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final Controller controller = router.getBackstack().get(router.getBackstackSize() - 1).controller();
        try {
            ((BaseController) controller).onConfigurationChange(newConfig);
        } catch (NullPointerException e) {
            Timber.d("An error occured when calling onConfigurationChange for controller: %s", controller);
        }
    }
}
