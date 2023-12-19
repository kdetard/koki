package io.github.kdetard.koki.keycloak.models;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.kdetard.koki.R;

public record KeycloakConfig(String authServerUrl, String realm, String client, String redirectUri) {
    public static KeycloakConfig getDefaultConfig(@NonNull Context context) {
        return new KeycloakConfig(
                context.getString(R.string.authServerUrl),
                context.getString(R.string.realm),
                context.getString(R.string.clientId),
                context.getString(R.string.redirectUri)
        );
    }
}
