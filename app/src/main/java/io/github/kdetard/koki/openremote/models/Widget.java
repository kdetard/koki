package io.github.kdetard.koki.openremote.models;

import com.squareup.moshi.Json;

import java.util.List;

public sealed interface Widget extends OpenRemoteObject permits LineChartWidget, MapWidget {
    String id();
    String displayName();

    @Override
    @Json(name = "widgetTypeId")
    String type();
}
