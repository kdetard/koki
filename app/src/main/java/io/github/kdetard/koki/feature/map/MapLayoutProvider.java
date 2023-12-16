package io.github.kdetard.koki.feature.map;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public interface MapLayoutProvider {
    MapView getMapView();
    MapboxMap getMapboxMap();
    void onMapReady();
}
