package io.github.kdetard.koki.openremote.models;

import com.squareup.moshi.Json;

import java.util.List;

public record LineChartWidget (
    String id,
    String displayName,
    Config config
) implements Widget {
    @Override
    @Json(name = "widgetTypeId")
    public String type() { return TYPE; }

    public static final String TYPE = "linechart";

    record Config(
        boolean showLegend,
        String displayName,
        List<Ref> attributeRefs
    ) { }

    record Ref(
        String id,
        String name
    ) {}
}
