package io.github.kdetard.koki.conductor;

import com.bluelinelabs.conductor.Controller;

public final class ControllerUtils {
    public static void watchForLeaks(Controller controller) {
        controller.addLifecycleListener(new RefWatchingControllerLifecycleListener());
    }
}
