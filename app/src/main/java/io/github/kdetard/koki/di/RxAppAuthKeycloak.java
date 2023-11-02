package io.github.kdetard.koki.di;

import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;

import io.github.kdetard.koki.model.Keycloak.KeycloakConfig;
import io.github.kdetard.koki.model.RxActivity;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;

public class RxAppAuthKeycloak extends RxKeycloak {
    public static Observable<AuthorizationService> service(final AppCompatActivity context) {
        return Observable.create(emitter -> {
            final AuthorizationService authService = new AuthorizationService(context);
            emitter.setCancellable(authService::dispose);
            emitter.onNext(authService);
        });
    }

    public static Single<KeycloakConfig> auth(final AppCompatActivity activity, final AuthorizationService service, final KeycloakConfig config, final int requestCode) {
        return fetchConfig(config)
                .onErrorResumeNext(ex -> {
                    Timber.w(ex, "An error occurred while performing OpenID endpoint discovery.");
                    return RxKeycloak.authConfig(config);
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
                    if (r.resultCode != AppCompatActivity.RESULT_OK) {
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