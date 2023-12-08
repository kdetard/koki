package io.github.kdetard.koki.openremote.models;

import java.util.List;

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
    ) implements AssetAttribute {}
}
