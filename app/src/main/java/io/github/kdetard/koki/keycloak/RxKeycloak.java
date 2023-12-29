package io.github.kdetard.koki.keycloak;

import android.net.Uri;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import io.github.kdetard.koki.keycloak.models.KeycloakConfig;
import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;

public abstract class RxKeycloak {
    public static Single<AuthorizationServiceConfiguration> fetchConfig(final KeycloakConfig config) {
        return Single.create(emitter ->
                AuthorizationServiceConfiguration.fetchFromUrl(
                        Uri.parse(config.authServerUrl() + "/realms/" + config.realm() + "/.well-known/openid-configuration"),
                        (serviceConfiguration, ex) -> {
                            if (serviceConfiguration != null) {
                                emitter.onSuccess(serviceConfiguration);
                            } else if (ex != null) {
                                emitter.onError(ex);
                            } else {
                                emitter.onError(new Error("Failed."));
                            }
                        })
        );
    }

    public static Single<AuthorizationServiceConfiguration> authConfig(final KeycloakConfig config) {
        return Single.just(new AuthorizationServiceConfiguration(
                Uri.parse(config.authServerUrl() + "/realms/" + config.realm() + "/protocol/openid-connect/auth"),
                Uri.parse(config.authServerUrl() + "/realms/" + config.realm() + "/protocol/openid-connect/token")
        ));
    }

    public static Single<AuthorizationRequest> buildRequest(final KeycloakConfig config) {
        return fetchConfig(config)
                .onErrorResumeNext(ex -> {
                    Timber.w(ex, "An error occurred while performing OpenID endpoint discovery. Using hardcoded ones.");
                    return authConfig(config);
                })
                .flatMap(authConfig ->
                        Single.just(new AuthorizationRequest.Builder(
                                authConfig,
                                config.client(),
                                ResponseTypeValues.CODE,
                                Uri.parse(config.redirectUri())
                        ).build())
                );
    }
}