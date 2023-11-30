package io.github.kdetard.koki.feature.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;

import android.content.res.Configuration;
import android.os.Bundle;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.tencent.mmkv.MMKV;

import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.databinding.ActivityMainBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.feature.home.HomeController;
import io.github.kdetard.koki.feature.onboard.OnboardController;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
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

        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(MMKV.defaultMMKV().getString("accessToken", "").isEmpty()
                    ? new OnboardController()
                    : new HomeController()));
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ((BaseController) router.getBackstack().get(router.getBackstackSize() - 1).controller()).onConfigurationChange(newConfig);
    }
}
