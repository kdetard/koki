package io.github.kdetard.koki.feature.onboard;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.reactivex.rxjava3.subjects.PublishSubject;

@Module
@InstallIn(SingletonComponent.class)
public class OnboardModule {
    @Provides
    @Singleton
    public static PublishSubject<OnboardEvent> provideOnboardEvent() {
        return PublishSubject.create();
    }
}
