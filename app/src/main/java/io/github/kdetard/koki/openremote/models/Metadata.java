package io.github.kdetard.koki.openremote.models;

public sealed interface Metadata permits AttributeMetadata, HTTPAgentAsset.DataMetadata {
    Boolean readOnly();

    Boolean ruleState();

    Boolean storeDataPoints();

    Boolean showOnDashboard();

    String label();
}
