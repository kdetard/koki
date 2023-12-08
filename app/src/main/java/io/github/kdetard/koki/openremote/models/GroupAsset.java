package io.github.kdetard.koki.openremote.models;

import java.util.List;

public record GroupAsset(
    String id,
    int version,
    long createdOn,
    String name,
    boolean accessPublicRead,
    String realm,
    List<String> path,
    GroupAsset.Attributes attributes
) implements Asset<GroupAsset.Attributes> {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = GroupAsset.class.getSimpleName();

    public record Attributes(
        Text notes,
        GeoJsonPoint location,
        ChildAssetType childAssetType
    ) implements AssetAttribute {}
}
