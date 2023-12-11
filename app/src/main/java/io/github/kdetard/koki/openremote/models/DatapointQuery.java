package io.github.kdetard.koki.openremote.models;

public record DatapointQuery(
    int amountOfPoints,
    long fromTimestamp,
    long toTimestamp,
    String type
) {
    public static String DEFAULT_TYPE = "lttb";
}
