package io.github.kdetard.koki.openremote.models;

import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.List;

import io.github.kdetard.koki.map.SymbolUtils;

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
    ) implements AssetAttribute {
        @Override
        public SymbolOptions toSymbol() {
            return location().value().toSymbol()
                .withIconImage(SymbolUtils.GROUP_SYMBOL);
        }

        @Override
        public Integer toIconResource() {
            return SymbolUtils.ICONS.get(SymbolUtils.GROUP_SYMBOL);
        }
    }
}
