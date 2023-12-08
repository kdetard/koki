package io.github.kdetard.koki.openremote.models;

import java.util.List;
import java.util.Map;

public record MultivaluedTextMap(
    long timestamp,
    Map<String, List<String>> value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "multivaluedTextMap";
}
