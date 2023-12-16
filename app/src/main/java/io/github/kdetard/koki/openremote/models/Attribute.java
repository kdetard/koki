package io.github.kdetard.koki.openremote.models;

public sealed interface Attribute extends OpenRemoteObject permits BooleanAttribute, ChildAssetType, ColourRGB, ConnectionStatus, ConsoleProviders, Direction, Email, GeoJsonPoint, HostAddress, HttpUrl, Json, JsonObject, MultivaluedTextMap, Number, PortNumber, PositiveInteger, PositiveNumber, Text, TextList {
    long timestamp();
    @Override
    String type();
}
