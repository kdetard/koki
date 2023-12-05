package io.github.kdetard.koki.di;

import android.content.Context;

import androidx.datastore.rxjava3.RxDataStore;
import androidx.datastore.rxjava3.RxDataStoreBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.datastore.SettingsSerializer;
import io.github.kdetard.koki.feature.onboard.OnboardEvent;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public static Flowable<OnboardEvent> provideOnboardEvent(final RxDataStore<Settings> settings) {
        return settings.data()
                .map(currentSettings -> {
                    final boolean isLoggedOut = currentSettings.getLoggedOut();
                    final boolean accessTokenIsEmpty = currentSettings.getAccessToken().isEmpty();
                    if (isLoggedOut || accessTokenIsEmpty) {
                        settings.updateDataAsync(newSettings ->
                                Single.just(newSettings.toBuilder().setLoggedOut(true).setAccessToken("").build()));
                        return OnboardEvent.LOGGED_OUT;
                    }
                    return OnboardEvent.LOGGED_IN;
                });
    }

    @Provides
    @Singleton
    public static SettingsSerializer provideSettingsSerializer() {
        return new SettingsSerializer();
    }

    @Provides
    @Singleton
    public static RxDataStore<Settings> provideSettings(
            final @ApplicationContext Context context,
            final SettingsSerializer settingsSerializer
    ) {
        return new RxDataStoreBuilder<>(
                context,
                "settings.pb",
                settingsSerializer
        ).build();
    }
}
