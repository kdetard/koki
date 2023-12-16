package io.github.kdetard.koki.openremote.models;

public record PortNumber(
    long timestamp,
    Integer value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "TCP_IPPortNumber";
}