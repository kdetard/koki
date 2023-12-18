package io.github.kdetard.koki.openremote.models;

import com.squareup.moshi.Json;

public record User(
    String realm,
    String realmId,
    String id,
    String firstName,
    String lastName,
    String email,
    boolean enabled,
    long createdOn,
    @Json(name = "serviceAccount") boolean isServiceAccount,
    String username
) {
}
