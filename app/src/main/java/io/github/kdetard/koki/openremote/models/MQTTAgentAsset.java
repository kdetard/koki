package io.github.kdetard.koki.openremote.models;

import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.List;

import io.github.kdetard.koki.map.SymbolUtils;

public record MQTTAgentAsset(
    String id,
    int version,
    long createdOn,
    String name,
    boolean accessPublicRead,
    String realm,
    List<String> path,
    MQTTAgentAsset.Attributes attributes
) implements Asset<MQTTAgentAsset.Attributes> {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "MQTTAgent";

    public record Attributes(
        Text notes,
        Text clientId,
        Text websocketPath,
        Text websocketQuery,
        BooleanAttribute agentDisabled,
        ConnectionStatus agentStatus,
        BooleanAttribute websocketMode,
        Text lastWillTopic,
        BooleanAttribute lastWillRetain,
        PortNumber port,
        BooleanAttribute resumeSession,
        HostAddress host,
        Json lastWillUpload,
        GeoJsonPoint location,
        BooleanAttribute secureMode
    ) implements AssetAttribute {
        @Override
        public SymbolOptions toSymbol() {
            return location().value().toSymbol()
                    .withIconImage(SymbolUtils.MQTT_SYMBOL);
        }

        @Override
        public Integer toIconResource() {
            return SymbolUtils.ICONS.get(SymbolUtils.MQTT_SYMBOL);
        }

        @Override
        public Integer toTintResource() { return SymbolUtils.TINTS.get(SymbolUtils.MQTT_SYMBOL); }
    }
}
