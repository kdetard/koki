package io.github.kdetard.koki.openremote.models;

import java.util.Map;

public record JsonObject<T extends Metadata>(
    long timestamp,
    Map<String, ?> value,
    T meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "JSONObject";
}
