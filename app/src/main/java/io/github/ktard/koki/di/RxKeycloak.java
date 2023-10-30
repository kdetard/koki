package io.github.ktard.koki.di;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;

import java.io.Serializable;

import io.github.ktard.koki.R;
import io.github.ktard.koki.model.RxActivity;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;

public class RxKeycloak {
    public static class Config {

        public final String authServerUrl;
        public final String realm;
        public final String client;
        public final String redirectUri;

        public Config(final String authServerUrl, final String realm, final String client, final String redirectUri) {
            this.authServerUrl = authServerUrl;
            this.realm = realm;
            this.client = client;
            this.redirectUri = redirectUri;
        }

        public static Config getDefaultConfig(@NonNull Context context) {
            return new RxKeycloak.Config(
                    context.getString(R.string.authServerUrl),
                    context.getString(R.string.realm),
                    context.getString(R.string.clientId),
                    context.getString(R.string.redirectUri)
            );
        }
    }

    public static final class Payload implements Serializable {

        public static class RealmAccess implements Serializable  {
            public String[] roles;
        }

        public static class Account implements Serializable  {
            public String[] roles;
        }

        public static class ResourceAccess implements Serializable  {
            public Account account;
        }

        public String acr;

        @SerializedName("allowed-origins")
        public String[] allowedOrigins;

        public String aud;

        @SerializedName("auth_time")
        public int authTime;

        public String azp;

        public String email;

        @SerializedName("email_verified")
        public boolean emailVerified;

        public int exp;

        @SerializedName("family_name")
        public String familyName;

        @SerializedName("given_name")
        public String givenName;

        public int iat;

        public String iss;

        public String jti;

        public String name;

        @SerializedName("preferred_username")
        public String preferredUsername;

        @SerializedName("realm_access")
        public RealmAccess realmAccess;

        @SerializedName("resource_access")
        public ResourceAccess resourceAccess;

        public String scope;

        @SerializedName("session_state")
        public String sessionState;

        public String sub;

        public String typ;

        public static Payload fromJson(final String json) {
            return new Gson().fromJson(json, Payload.class);
        }
    }

    public static class JWT {
        public final String header;
        public final String body;
        public final String signature;

        public JWT(final String accessToken) {
            final String[] tokenParts = accessToken.split("\\.");
            this.header = new String(Base64.decode(tokenParts[0], Base64.DEFAULT));
            this.body = new String(Base64.decode(tokenParts[1], Base64.DEFAULT));
            this.signature = tokenParts[2];
        }
    }

    //Auth Streams:
    public static Single<AuthorizationServiceConfiguration> fetchConfig(final Config config) {
        return Single.create(emitter ->
                AuthorizationServiceConfiguration.fetchFromUrl(
                        Uri.parse(config.authServerUrl + "/realms/" + config.realm + "/.well-known/openid-configuration"),
                        (serviceConfiguration, ex) -> {
                            if(serviceConfiguration != null) {
                                emitter.onSuccess(serviceConfiguration);
                            } else if(ex != null) {
                                emitter.onError(ex);
                            } else {
                                emitter.onError(new Error("Failed."));
                            }
                        })
        );
    }

    public static Single<AuthorizationServiceConfiguration> authConfig(final Config config) {
        return Single.just(new AuthorizationServiceConfiguration(
                Uri.parse(config.authServerUrl + "/realms/" + config.realm + "/protocol/openid-connect/auth"),
                Uri.parse(config.authServerUrl + "/realms/" + config.realm + "/protocol/openid-connect/token")
        ));
    }

    public static Observable<AuthorizationService> service(final AuthorizationService authService) {
        return Observable.create(emitter -> {
            emitter.setCancellable(authService::dispose);
            emitter.onNext(authService);
        });
    }

    public static Single<Config> auth(final Activity activity, final AuthorizationService service, final Config config, final int requestCode) {
        return fetchConfig(config)
                .onErrorResumeNext(ex -> {
                    Timber.w(ex, "An error occurred while performing OpenID endpoint discovery.");
                    return authConfig(config);
                })
                .flatMap(authConfig ->
                        Single.just(new AuthorizationRequest.Builder(
                                authConfig,
                                config.client,
                                ResponseTypeValues.CODE,
                                Uri.parse(config.redirectUri)
                        ).build())
                )
                .flatMap(authRequest -> Single.create(emitter -> {
                    final CustomTabsIntent customTabsIntent = service.createCustomTabsIntentBuilder()
                            .setInitialActivityHeightPx(500)
                            .setCloseButtonPosition(CustomTabsIntent.CLOSE_BUTTON_POSITION_END)
                            .build();
                    final Intent authIntent = service.getAuthorizationRequestIntent(authRequest, customTabsIntent);
                    activity.startActivityForResult(authIntent, requestCode);
                    emitter.onSuccess(config);
                }));
    }

    public static Single<TokenResponse> exchangeTokens(final AuthorizationService authService, final AuthorizationResponse response) {
        return Single.create(emitter -> authService.performTokenRequest(
                response.createTokenExchangeRequest(),
                (resp, ex) -> {
                    if (resp != null) {
                        emitter.onSuccess(resp);
                    } else {
                        emitter.onError(ex);
                    }
                }
        ));
    }

    public static Maybe<AuthorizationResponse> authResult(final Observable<RxActivity.ActivityResult> onActivityResult, final int requestCode) {
        return onActivityResult.firstOrError()
                .filter(r -> r.requestCode == requestCode)
                .flatMapSingle(r -> {
                    if (r.resultCode != Activity.RESULT_OK) {
                        return Single.error(new Exception("User Abandoned Auth."));
                    } else {
                        return Single.just(r);
                    }
                })
                .flatMapSingle(r -> {
                    final AuthorizationException ex = AuthorizationException.fromIntent(r.data);
                    final AuthorizationResponse response = AuthorizationResponse.fromIntent(r.data);
                    if (ex != null) {
                        return Single.error(ex);
                    } else {
                        assert response != null;
                        return Single.just(response);
                    }
                }) ;
    }
}