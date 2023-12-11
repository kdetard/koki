package io.github.kdetard.koki.aqicn.models;

import java.util.List;

public record AqicnResponse(
    String status,
    Data data,
    String message
) {
    public record Data(
        String idx,
        float aqi,
        Time time,
        City city,
        String dominentpol,
        IAQI iaqi,
        List<Attribution> attributions,
        Forecast forecast
    ) {}

    public record Time(
        String s,
        String tz
    ) {}

    public record City(
        String name,
        String url,
        List<Float> geo
    ) {}

    public record IAQI(
        AQIValue pm25
    ) {}

    public record Attribution(
        String url,
        String name
    ) {}

    public record AQIValue(
        String v
    ) {}

    public record Forecast(
        Daily daily
    ) {}

    public record Daily(
        List<ForecastDay> pm25,
        List<ForecastDay> pm10,
        List<ForecastDay> o3,
        List<ForecastDay> uvi
    ) {}

    public record ForecastDay(
        String avg,
        String day,
        String max,
        String min
    ) {}

    public static final String OVER_QUOTA = "Over quota";
    public static final String OK = "ok";
    public static final String INVALID_KEY = "Invalid key";
    public static final String UNKNOWN_CITY = "Unknown city";
}
