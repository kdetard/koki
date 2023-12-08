package io.github.kdetard.koki.openremote.models;

public record ColourRGB(
    long timestamp,
    Integer value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "colourRGB";
}
