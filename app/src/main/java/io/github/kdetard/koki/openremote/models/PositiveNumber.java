package io.github.kdetard.koki.openremote.models;

public record PositiveNumber(
    long timestamp,
    Float value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "positiveNumber";
}
