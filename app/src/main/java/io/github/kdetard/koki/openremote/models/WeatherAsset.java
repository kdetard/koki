package io.github.kdetard.koki.openremote.models;

import java.util.List;

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
        PositiveNumber rainfall,
        Text notes,
        PositiveNumber uVIndex,
        PositiveNumber sunAzimuth,
        PositiveNumber sunZenith,
        TextList tags,
        Text manufacturer,
        Number temperature,
        PositiveInteger humidity,
        GeoJsonPoint location,
        Text place,
        Direction windDirection,
        PositiveNumber windSpeed,
        PositiveNumber sunAltitude
    ) implements AssetAttribute {}
}
