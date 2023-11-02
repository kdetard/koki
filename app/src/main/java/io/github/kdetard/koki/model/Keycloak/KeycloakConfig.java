package io.github.kdetard.koki.model.Keycloak;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.kdetard.koki.R;

public class KeycloakConfig {

    public final String authServerUrl;
    public final String realm;
    public final String client;
    public final String redirectUri;

    public KeycloakConfig(final String authServerUrl, final String realm, final String client, final String redirectUri) {
        this.authServerUrl = authServerUrl;
        this.realm = realm;
        this.client = client;
        this.redirectUri = redirectUri;
    }

    public static KeycloakConfig getDefaultConfig(@NonNull Context context) {
        return new KeycloakConfig(
                context.getString(R.string.authServerUrl),
                context.getString(R.string.realm),
                context.getString(R.string.clientId),
                context.getString(R.string.redirectUri)
        );
    }
}
