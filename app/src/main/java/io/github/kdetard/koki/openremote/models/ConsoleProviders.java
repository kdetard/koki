package io.github.kdetard.koki.openremote.models;

import java.util.Map;

public record ConsoleProviders(
    long timestamp,
    Map<String, AdditionalProperties> value,
    AttributeMetadata meta
) implements Attribute {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "consoleProviders";

    public static class AdditionalProperties {
        public String version;
        public boolean requiresPermission;
        public boolean hasPermission;
        public boolean success;
        public boolean enabled;
        public boolean disabled;
        public Map<String, ?> data;
    }
}
