package io.github.kdetard.koki.openremote.models;

public record Direction(
    long timestamp,
    Float value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "direction";
}
