package io.github.kdetard.koki.openremote.models;

public sealed interface AssetAttribute permits ConsoleAsset.Attributes, GroupAsset.Attributes, HTTPAgentAsset.Attributes, LightAsset.Attributes, MQTTAgentAsset.Attributes, WeatherAsset.Attributes {
    GeoJsonPoint location();
}
