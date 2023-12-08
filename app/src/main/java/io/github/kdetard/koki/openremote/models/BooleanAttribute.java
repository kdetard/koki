package io.github.kdetard.koki.openremote.models;

public record BooleanAttribute(
    long timestamp,
    Boolean value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "boolean";
}
