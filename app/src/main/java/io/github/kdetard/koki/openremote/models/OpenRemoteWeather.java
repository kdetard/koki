package io.github.kdetard.koki.openremote.models;

import com.squareup.moshi.Json;

import java.util.List;

public record OpenRemoteWeather(
    long dt,
    int id,
    int cod,
    Sys sys,
    String base,
    Main main,
    String name,
    Wind wind,
    @Json(name = "coord") Coordinate coordinate,
    Clouds clouds,
    List<Weather> weather,
    int timezone,
    int visibility
) {
    public record Sys(int id, int type, long sunset, String country, long sunrise) {
    }

    public record Main(
        double temp,
        int humidity,
        int pressure,
        @Json(name = "temp_max") double tempMax,
        @Json(name = "temp_min") double tempMin,
        @Json(name = "feels_like") double feelsLike
    ) {
    }

    public record Wind(@Json(name = "deg") int degree, double speed) {
    }

    public record Coordinate(double lat, double lon) {
    }

    public record Clouds(int all) {
    }

    public record Weather(int id, String icon, String main, String description) {
    }
}

