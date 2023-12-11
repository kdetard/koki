package io.github.kdetard.koki.feature.base;

import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bluelinelabs.conductor.Router;

import io.github.kdetard.koki.controller.OnConfigurationChangeListener;
import io.github.kdetard.koki.controller.OnLowMemoryListener;

public class BaseActivity extends AppCompatActivity {
    protected Router router;

    // clean up the listener: https://stackoverflow.com/a/63713958
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (router == null) return;
        findViewById(android.R.id.content).getViewTreeObserver().removeOnDrawListener(router::hasRootController);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (router == null) return;

        router.getBackstack()
                .stream()
                .filter(c -> c.controller() instanceof OnLowMemoryListener)
                .forEach(c -> ((OnLowMemoryListener) c.controller()).onLowMemory());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (router == null) return;

        router.getBackstack()
                .stream()
                .filter(c -> c.controller() instanceof OnConfigurationChangeListener)
                .forEach(c -> ((OnConfigurationChangeListener) c.controller()).onConfigurationChange(newConfig));
    }
}
