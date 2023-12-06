package io.github.kdetard.koki.feature.base;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;

import autodispose2.OutsideScopeException;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ControllerLifecycleSubjectHelper {
    private ControllerLifecycleSubjectHelper() { }

    @NonNull
    public static BehaviorSubject<ControllerEvent> create(Controller controller) {
        ControllerEvent initialState;
        if (controller.isBeingDestroyed() || controller.isDestroyed()) {
            throw new OutsideScopeException("Cannot bind to Controller lifecycle when outside of it.");
        } else if (controller.isAttached()) {
            initialState = ControllerEvent.ATTACH;
        } else if (controller.getView() != null) {
            initialState = ControllerEvent.CREATE_VIEW;
        } else if (controller.getActivity() != null) {
            initialState = ControllerEvent.CONTEXT_AVAILABLE;
        } else {
            initialState = ControllerEvent.CREATE;
        }

        final var subject = BehaviorSubject.createDefault(initialState);

        controller.addLifecycleListener(new Controller.LifecycleListener() {
            @Override
            public void preContextAvailable(@NonNull Controller controller) {
                subject.onNext(ControllerEvent.CONTEXT_AVAILABLE);
            }

            @Override
            public void preCreateView(@NonNull Controller controller) {
                subject.onNext(ControllerEvent.CREATE_VIEW);
            }

            @Override
            public void preAttach(@NonNull Controller controller, @NonNull View view) {
                subject.onNext(ControllerEvent.ATTACH);
            }

            @Override
            public void preDetach(@NonNull Controller controller, @NonNull View view) {
                subject.onNext(ControllerEvent.DETACH);
            }

            @Override
            public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
                subject.onNext(ControllerEvent.DESTROY_VIEW);
            }

            @Override
            public void preContextUnavailable(@NonNull Controller controller, @NonNull Context context) {
                subject.onNext(ControllerEvent.CONTEXT_UNAVAILABLE);
            }

            @Override
            public void preDestroy(@NonNull Controller controller) {
                subject.onNext(ControllerEvent.DESTROY);
            }
        });

        return subject;
    }
}