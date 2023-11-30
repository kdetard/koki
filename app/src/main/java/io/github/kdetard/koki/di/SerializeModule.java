package io.github.kdetard.koki.di;

import com.squareup.moshi.Moshi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.network.models.CookieJsonAdapter;

@Module
@InstallIn(SingletonComponent.class)
public class SerializeModule {
    @Provides
    @Singleton
    public static Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(new CookieJsonAdapter())
                .build();
    }
}
