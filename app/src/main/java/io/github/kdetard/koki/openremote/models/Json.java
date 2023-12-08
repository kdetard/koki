package io.github.kdetard.koki.openremote.models;

import java.util.Map;

public record Json(
    long timestamp,
    Map<String, Object> value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "JSON";
}