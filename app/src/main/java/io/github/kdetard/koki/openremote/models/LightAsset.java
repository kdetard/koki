package io.github.kdetard.koki.openremote.models;

import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.List;

import io.github.kdetard.koki.map.SymbolUtils;

public record LightAsset(
    String id,
    int version,
    long createdOn,
    String name,
    boolean accessPublicRead,
    String realm,
    List<String> path,
    LightAsset.Attributes attributes
) implements Asset<LightAsset.Attributes> {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = LightAsset.class.getSimpleName();

    public record Attributes(
        Text notes,
        PositiveInteger brightness,
        PositiveInteger colourTemperature,
        GeoJsonPoint location,
        Email email,
        TextList tags,
        BooleanAttribute onOff
    ) implements AssetAttribute {
        @Override
        public SymbolOptions toSymbol() {
            return location().value().toSymbol()
                    .withIconImage(SymbolUtils.LIGHT_SYMBOL);
        }

        @Override
        public Integer toIconResource() {
            return SymbolUtils.ICONS.get(SymbolUtils.LIGHT_SYMBOL);
        }
    }

    public record LightAgentLink(
        String id
    ) implements OpenRemoteObject {
        @Override
        public String type() { return TYPE; }

        public static final String TYPE = "MQTTAgentLink";
    }
}