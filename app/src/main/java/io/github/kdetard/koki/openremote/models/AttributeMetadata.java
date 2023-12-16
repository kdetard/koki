package io.github.kdetard.koki.openremote.models;

public record AttributeMetadata(
    Boolean readOnly,

    Boolean ruleState,

    Boolean storeDataPoints,

    Boolean showOnDashboard,

    String label
) implements Metadata {
}
