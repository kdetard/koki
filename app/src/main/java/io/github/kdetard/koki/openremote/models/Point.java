package io.github.kdetard.koki.openremote.models;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.List;

import io.github.kdetard.koki.map.SymbolUtils;

public record Point(
    long timestamp,
    List<Float> coordinates,
    AttributeMetadata meta
) implements OpenRemoteObject {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "Point";

    public Float latitude() { return coordinates.get(1); }

    public Float longitude() { return coordinates.get(0); }

    public SymbolOptions toSymbol() {
        return SymbolUtils.defaultSymbolOptions()
                .withLatLng(new LatLng(latitude(), longitude()));
    }

    public LatLng toLatLng() { return new LatLng(latitude(), longitude()); }
}
