package io.github.kdetard.koki.openremote.models;

import java.util.List;

public record TextList(
    long timestamp,
    List<String> value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = String.format("%s[]", Text.TYPE);
}
