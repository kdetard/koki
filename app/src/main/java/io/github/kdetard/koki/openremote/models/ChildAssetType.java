package io.github.kdetard.koki.openremote.models;

public record ChildAssetType(
    long timestamp,
    String value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "assetType";
}
