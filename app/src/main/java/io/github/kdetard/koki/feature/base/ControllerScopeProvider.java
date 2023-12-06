package io.github.kdetard.koki.feature.base;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;

import autodispose2.OutsideScopeException;
import autodispose2.lifecycle.CorrespondingEventsFunction;
import autodispose2.lifecycle.LifecycleScopeProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ControllerScopeProvider implements LifecycleScopeProvider<ControllerEvent> {
    private static final CorrespondingEventsFunction<ControllerEvent> CORRESPONDING_EVENTS =
            lastEvent -> switch (lastEvent) {
                case CREATE, DETACH -> ControllerEvent.DESTROY;
                case CONTEXT_AVAILABLE -> ControllerEvent.CONTEXT_UNAVAILABLE;
                case CREATE_VIEW -> ControllerEvent.DESTROY_VIEW;
                case ATTACH -> ControllerEvent.DETACH;
                default ->
                        throw new OutsideScopeException("Cannot bind to Controller lifecycle when outside of it.");
            };

    @NonNull
    private final BehaviorSubject<ControllerEvent> lifecycleSubject;

    public static ControllerScopeProvider from(@NonNull Controller controller) {
        return new ControllerScopeProvider(controller);
    }

    private ControllerScopeProvider(@NonNull Controller controller) {
        lifecycleSubject = ControllerLifecycleSubjectHelper.create(controller);
    }

    @Override
    public Observable<ControllerEvent> lifecycle() {
        return lifecycleSubject.hide();
    }

    @Override
    public CorrespondingEventsFunction<ControllerEvent> correspondingEvents() {
        return CORRESPONDING_EVENTS;
    }

    @Override
    public ControllerEvent peekLifecycle() {
        return lifecycleSubject.getValue();
    }
}
