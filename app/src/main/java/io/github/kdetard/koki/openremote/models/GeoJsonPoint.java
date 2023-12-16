package io.github.kdetard.koki.openremote.models;

public record GeoJsonPoint(
    long timestamp,
    Point value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "GEO_JSONPoint";
}
