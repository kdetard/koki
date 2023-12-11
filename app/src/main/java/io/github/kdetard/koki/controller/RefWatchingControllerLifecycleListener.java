package io.github.kdetard.koki.controller;

import android.view.View;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;

import leakcanary.AppWatcher;

public class RefWatchingControllerLifecycleListener extends Controller.LifecycleListener {
    private boolean hasExited = false;

    @Override
    public void postDestroy(@NonNull Controller controller) {
        if (hasExited) {
            expectWeaklyReachable(controller);
        }
    }

    @Override
    public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
        AppWatcher.INSTANCE.getObjectWatcher().expectWeaklyReachable(
                view,
                "A destroyed controller view should have only weak references."
        );
    }

    @Override
    public void onChangeEnd(
            Controller controller,
            @NonNull ControllerChangeHandler changeHandler,
            ControllerChangeType changeType
    ) {
        hasExited = !changeType.isEnter;
        if (controller.isDestroyed()) {
            expectWeaklyReachable(controller);
        }
    }

    private void expectWeaklyReachable(Controller controller) {
        AppWatcher.INSTANCE.getObjectWatcher().expectWeaklyReachable(
                controller,
                "A destroyed controller should have only weak references."
        );
    }
}
