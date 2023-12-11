package io.github.kdetard.koki.feature.map;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

public interface MapLayoutProvider {
    MapView getMapView();
    void onMapReady(MapboxMap mapboxMap, Style style);
}
