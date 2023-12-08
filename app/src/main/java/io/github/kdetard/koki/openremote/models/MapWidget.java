package io.github.kdetard.koki.openremote.models;

import java.util.List;

public record MapWidget(
    List<Double> center,
    String id,
    String displayName
) implements Widget {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "map";
}
