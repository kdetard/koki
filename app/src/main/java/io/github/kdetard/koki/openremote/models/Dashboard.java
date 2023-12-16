package io.github.kdetard.koki.openremote.models;

import androidx.annotation.Nullable;

import java.util.List;

public record Dashboard(
    String id,
    long createdOn,
    String realm,
    long version,
    String ownerId,
    String viewAccess,
    String editAccess,
    String displayName,
    Template template
) {
    public record Template(
        String id,
        long columns,
        long maxScreenWidth,
        @Nullable
        List<ScreenPreset> screenPresets,
        @Nullable
        List<Widget> widgets
    ) { }

    public record ScreenPreset(
        String id,
        String displayName,
        long breakpoint,
        String scalingPreset
    ) {}
}
