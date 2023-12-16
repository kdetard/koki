package io.github.kdetard.koki.feature.map;

import com.mapbox.mapboxsdk.maps.MapboxMap;

public interface OnMapListener {
    default void onMapReady(MapboxMap mapboxMap) {}
    default void onSymbolClick(MapboxMap mapboxMap) {}
}
