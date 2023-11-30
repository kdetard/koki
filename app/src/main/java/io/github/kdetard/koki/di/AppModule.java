package io.github.kdetard.koki.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.datastore.AuthStatus;
import io.reactivex.rxjava3.core.Flowable;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    /*@Provides
    @Singleton
    public static DataStore<Settings> provideSettings(
            final @ApplicationContext Context context,
            final Moshi moshi
    ) {
        return DataStoreFactory.INSTANCE.create(
                moshi.adapter(Settings.class),
                () -> DataStoreFile.dataStoreFile(context, "settings.json")
        );
    }*/

    @Provides
    @Singleton
    public static Flowable<AuthStatus> provideAuthStatus() {
        return Flowable.empty();
    }
}
