package io.github.kdetard.koki.openremote.models;

import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.Locale;
import java.util.Random;

import io.github.kdetard.koki.map.SymbolUtils;

public sealed interface AssetAttribute permits ConsoleAsset.Attributes, GroupAsset.Attributes, HTTPAgentAsset.Attributes, LightAsset.Attributes, MQTTAgentAsset.Attributes, WeatherAsset.Attributes {
    GeoJsonPoint location();
    default SymbolOptions toSymbol() {
        var rnd = new Random();
        return location().value().toSymbol()
                .withIconColor(String.format(Locale.getDefault(), "rgba(%d, %d, %d, %d)", rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255), 0));
    }

    default Integer toIconResource() {
        return SymbolUtils.ICONS.get(SymbolUtils.DEFAULT_SYMBOL);
    }
}
