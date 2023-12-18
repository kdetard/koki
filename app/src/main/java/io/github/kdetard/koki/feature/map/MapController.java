package io.github.kdetard.koki.feature.map;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

import java.util.Objects;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.conductor.OnLowMemoryListener;
import io.github.kdetard.koki.map.SatelliteStyleBuilder;
import io.github.kdetard.koki.map.StreetsDarkStyleBuilder;
import io.github.kdetard.koki.map.StreetsLightStyleBuilder;

public abstract class MapController extends BaseController implements OnLowMemoryListener, MapLayoutProvider {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface MapEntryPoint {
        @StreetsLightStyleBuilder
        Style.Builder streetsLightStyleBuilder();
        @StreetsDarkStyleBuilder
        Style.Builder streetsDarkStyleBuilder();
        @SatelliteStyleBuilder
        Style.Builder satelliteStyleBuilder();
    }

    MapboxMap mapboxMap;

    MapEntryPoint entryPoint;

    SymbolManager symbolManager;

    Style.Builder styleBuilder;

    public MapController(int layoutRes) { super(layoutRes); }

    public MapController(int layoutRes, @Nullable Bundle args) { super(layoutRes, args); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), MapEntryPoint.class);

        var nightModeFlags = view.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES -> styleBuilder = entryPoint.streetsDarkStyleBuilder();
            case Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> styleBuilder = entryPoint.streetsLightStyleBuilder();
        }

        getMapView().getMapAsync(mapboxMap -> mapboxMap.setStyle(styleBuilder, style -> {
            this.mapboxMap = mapboxMap;
            if (getMapView() != null)
                symbolManager = new SymbolManager(getMapView(), mapboxMap, style);

            if (getSymbolManager() != null) {
                getSymbolManager().removeClickListener(this::onSymbolClick);
                getSymbolManager().addClickListener(this::onSymbolClick);
            }
            onMapReady();
        }));
    }

    public SymbolManager getSymbolManager() { return symbolManager; }
    public MapboxMap getMapboxMap() { return mapboxMap; }

    public boolean onSymbolClick(@NonNull Symbol symbol) { return false; }

    @Override
    protected void onActivityStarted(@NonNull Activity activity) {
        super.onActivityStarted(activity);
        getMapView().onStart();
    }

    @Override
    protected void onActivityResumed(@NonNull Activity activity) {
        super.onActivityResumed(activity);
        getMapView().onResume();
    }

    @Override
    protected void onActivityPaused(@NonNull Activity activity) {
        super.onActivityPaused(activity);
        getMapView().onPause();
    }

    @Override
    protected void onActivityStopped(@NonNull Activity activity) {
        super.onActivityStopped(activity);
        getMapView().onStop();
    }

    @Override
    public void onLowMemory() {
        getMapView().onLowMemory();
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        if (getSymbolManager() != null) {
            getSymbolManager().removeClickListener(this::onSymbolClick);
            getSymbolManager().onDestroy();
        }
        if (getMapView() != null) {
            getMapView().onDestroy();
        }
        super.onDestroyView(view);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getMapView().onSaveInstanceState(outState);
    }
}
