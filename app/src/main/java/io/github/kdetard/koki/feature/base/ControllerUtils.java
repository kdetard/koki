package io.github.kdetard.koki.feature.base;

import com.bluelinelabs.conductor.Controller;

public final class ControllerUtils {
    public static void watchForLeaks(Controller controller) {
        controller.addLifecycleListener(new RefWatchingControllerLifecycleListener());
    }
}
