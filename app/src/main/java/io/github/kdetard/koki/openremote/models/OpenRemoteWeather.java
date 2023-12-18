package io.github.kdetard.koki.openremote.models;

import java.util.List;

public record  OpenRemoteWeather(
        long dt,
        int id,
        int cod,
        Sys sys,
        String base,
        Main main,
        String name,
        Wind wind,
        Coord coord,
        Clouds clouds,
        List<Weather> weather,
        int timezone,
        int visibility
) {
    public record Sys(int id, int type, long sunset, String country, long sunrise) {
    }

    public record Main(double temp, int humidity, int pressure, double temp_max, double temp_min, double feels_like) {
    }

    public record Wind(int deg, double speed) {
    }

    public record Coord(double lat, double lon) {
    }

    public record Clouds(int all) {
    }

    public record Weather(int id, String icon, String main, String description) {
    }
}