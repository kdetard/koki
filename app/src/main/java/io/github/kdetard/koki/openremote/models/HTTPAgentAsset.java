package io.github.kdetard.koki.openremote.models;

import java.util.List;
import java.util.Map;

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
    ) implements AssetAttribute {}

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
