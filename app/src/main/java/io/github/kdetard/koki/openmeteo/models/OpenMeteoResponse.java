package io.github.kdetard.koki.openmeteo.models;

import com.squareup.moshi.Json;

public record OpenMeteoResponse(
    float latitude,
    float longitude,
    @Json(name = "generationtime_ms") float generationTime,
    @Json(name = "utc_offset_seconds") float utcOffset,
    String timezone,
    @Json(name = "timezone_abbreviation") String timezoneShort,
    long elevation,
    @Json(name = "current_units") Units currentUnits,
    Current current
) {
    public record Units(
        String time,
        String interval,
        @Json(name = "temperature_2m") String temperature2m,
        @Json(name = "relative_humidity_2m") String relativeHumidity2m,
        @Json(name = "apparent_temperature") String apparentTemperature,
        String rain
    ) {}

    public record Current(
        String time,
        long interval,
        @Json(name = "temperature_2m") float temperature2m,
        @Json(name = "relative_humidity_2m") float relativeHumidity2m,
        @Json(name = "apparent_temperature") float apparentTemperature,
        float rain
    ) {}
}
