package io.github.kdetard.koki.openremote.models;

import com.squareup.moshi.Json;

public record Datapoint(
    @Json(name = "x") long timestamp,
    @Json(name = "y") float value
) {}
