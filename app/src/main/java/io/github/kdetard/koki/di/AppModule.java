package io.github.kdetard.koki.di;

import android.content.Context;

import androidx.datastore.rxjava3.RxDataStore;
import androidx.datastore.rxjava3.RxDataStoreBuilder;

import com.squareup.moshi.JsonAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.datastore.SettingsSerializer;
import io.github.kdetard.koki.feature.onboard.OnboardEvent;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public static Flowable<OnboardEvent> provideOnboardEvent(
            final JsonAdapter<KeycloakToken> keycloakTokenJsonAdapter,
            final RxDataStore<Settings> settings
    ) {
        return settings.data()
                .map(Settings::getKeycloakTokenJson)
                .map(json -> json.isEmpty() ? "{}" : json)
                .map(keycloakTokenJsonAdapter::fromJson)
                .map(keycloakToken -> keycloakToken == null || keycloakToken.accessToken == null ? OnboardEvent.LOGGED_OUT : OnboardEvent.LOGGED_IN);
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
