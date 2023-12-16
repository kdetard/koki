package io.github.kdetard.koki.openremote.models;

import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.squareup.moshi.Json;

import java.util.List;

import io.github.kdetard.koki.map.SymbolUtils;

public record WeatherAsset(
    String id,
    int version,
    long createdOn,
    String name,
    boolean accessPublicRead,
    String realm,
    List<String> path,
    WeatherAsset.Attributes attributes
) implements Asset<WeatherAsset.Attributes> {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = WeatherAsset.class.getSimpleName();

    public record Attributes(
        PositiveNumber sunIrradiance,
        @Json(name = "NO") Number noIndex,
        PositiveNumber rainfall,
        Text notes,
        PositiveNumber uVIndex,
        @Json(name = "O3") Number ozoneIndex,
        @Json(name = "PM25") Number pm25Index,
        PositiveNumber sunAzimuth,
        @Json(name = "CO2") Number co2Index,
        @Json(name = "CO2_average") Number averageCo2Index,
        PositiveNumber sunZenith,
        @Json(name = "NO2") Number no2Index,
        @Json(name = "AQI_predict") Number aqiPredictIndex,
        @Json(name = "SO2") Number so2Index,
        TextList tags,
        Text manufacturer,
        Number temperature,
        @Json(name = "AQI") Number aqiIndex,
        @Json(name = "PM10") Number pm10Index,
        PositiveInteger humidity,
        GeoJsonPoint location,
        Text place,
        Direction windDirection,
        PositiveNumber windSpeed,
        PositiveNumber sunAltitude
    ) implements AssetAttribute {
        @Override
        public SymbolOptions toSymbol() {
            return location().value().toSymbol()
                .withIconImage(SymbolUtils.WEATHER_SYMBOL);
        }

        @Override
        public Integer toIconResource() { return SymbolUtils.ICONS.get(SymbolUtils.WEATHER_SYMBOL); }

        @Override
        public Integer toTintResource() { return SymbolUtils.TINTS.get(SymbolUtils.WEATHER_SYMBOL); }
    }
}
