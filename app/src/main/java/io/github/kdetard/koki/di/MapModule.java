package io.github.kdetard.koki.di;

import android.content.Context;

import com.mapbox.mapboxsdk.maps.Style;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.BuildConfig;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.map.SatelliteStyleBuilder;
import io.github.kdetard.koki.map.StreetsDarkStyleBuilder;
import io.github.kdetard.koki.map.StreetsLightStyleBuilder;
import io.github.kdetard.koki.map.SymbolUtils;

@Module
@InstallIn(SingletonComponent.class)
public abstract class MapModule {
    private static final String STYLE_URL_FORMAT = "%s?key=%s";

    @Provides
    @Singleton
    @StreetsLightStyleBuilder
    public static Style.Builder provideStreetLightStyleBuilder(final @ApplicationContext Context context) {
        return SymbolUtils.toBuilder(context)
                .fromUri(String.format(STYLE_URL_FORMAT, context.getString(R.string.map_style_streets_light), BuildConfig.MAPTILER_API_KEY));
    }

    @Provides
    @Singleton
    @StreetsDarkStyleBuilder
    public static Style.Builder provideStreetDarkStyleBuilder(final @ApplicationContext Context context) {
        return SymbolUtils.toBuilder(context)
                .fromUri(String.format(STYLE_URL_FORMAT, context.getString(R.string.map_style_streets_dark), BuildConfig.MAPTILER_API_KEY));
    }

    @Provides
    @Singleton
    @SatelliteStyleBuilder
    public static Style.Builder provideSatelliteStyleBuilder(final @ApplicationContext Context context) {
        return SymbolUtils.toBuilder(context)
                .fromUri(String.format(STYLE_URL_FORMAT, context.getString(R.string.map_style_satellite), BuildConfig.MAPTILER_API_KEY));
    }
}
