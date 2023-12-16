package io.github.kdetard.koki.openremote.models;

import java.util.List;

public record ConsoleAsset(
    String id,
    int version,
    long createdOn,
    String name,
    boolean accessPublicRead,
    String realm,
    List<String> path,
    Attributes attributes
) implements Asset<ConsoleAsset.Attributes> {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = ConsoleAsset.class.getSimpleName();

    public record Attributes(
        Text consoleVersion,
        Text consolePlatform,
        Text notes,
        ConsoleProviders consoleProviders,
        Text consoleName,
        GeoJsonPoint location
    ) implements AssetAttribute {}
}
