package io.github.kdetard.koki.openremote.models;

import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.List;

import io.github.kdetard.koki.map.SymbolUtils;

public record HTTPAgentAsset(
    String id,
    int version,
    long createdOn,
    String name,
    boolean accessPublicRead,
    String realm,
    List<String> path,
    HTTPAgentAsset.Attributes attributes
) implements Asset<HTTPAgentAsset.Attributes> {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "HTTPAgent";

    public record Attributes(
        MultivaluedTextMap requestQueryParameters,
        HttpUrl baseURL,
        PositiveInteger requestTimeoutMillis,
        Text notes,
        MultivaluedTextMap requestHeaders,
        JsonObject<DataMetadata> data,
        BooleanAttribute followRedirects,
        GeoJsonPoint location,
        BooleanAttribute agentDisabled,
        ConnectionStatus agentStatus,
        PositiveInteger pollingMillis
    ) implements AssetAttribute {
        @Override
        public SymbolOptions toSymbol() {
            return location().value().toSymbol()
                .withIconImage(SymbolUtils.HTTP_SYMBOL);
        }

        @Override
        public Integer toIconResource() {
            return SymbolUtils.ICONS.get(SymbolUtils.HTTP_SYMBOL);
        }

        @Override
        public Integer toTintResource() { return SymbolUtils.TINTS.get(SymbolUtils.HTTP_SYMBOL); }
    }

    public record DataMetadata(
        Boolean readOnly,
        Boolean ruleState,
        Boolean storeDataPoints,
        Boolean showOnDashboard,
        String label,
        HttpAgentLink agentLink,
        List<HttpAgentAttributeLink> attributeLinks
    ) implements Metadata {}

    public record HttpAgentLink(
        String id,
        Long pollingMillis,
        String path,
        String method,
        Boolean messageConvertBinary,
        Boolean messageConvertHex
    ) implements OpenRemoteObject {
        @Override
        public String type() { return TYPE; }
        public static final String TYPE = "HTTPAgentLink";
    }

    public record HttpAgentAttributeLink(
        Ref ref,
        List<JsonPath> filters
    ) {}

    public record Ref(
        String id,
        String name
    ) {}
}
