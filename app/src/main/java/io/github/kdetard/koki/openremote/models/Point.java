package io.github.kdetard.koki.openremote.models;

import java.util.List;

public record Point(
    long timestamp,
    List<Float> coordinates,
    AttributeMetadata meta
) implements OpenRemoteObject {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = Point.class.getSimpleName();
}
