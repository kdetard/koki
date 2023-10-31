package io.github.ktard.koki.di;

import android.net.Uri;

import com.tencent.mmkv.MMKV;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Objects;

import dagger.hilt.android.scopes.ActivityScoped;
import io.github.ktard.koki.model.Keycloak.KeycloakConfig;
import io.github.ktard.koki.model.Keycloak.KeycloakGrantType;
import io.github.ktard.koki.model.Keycloak.KeycloakToken;
import io.github.ktard.koki.network.KeycloakApiService;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import timber.log.Timber;

@ActivityScoped
public class RxRestKeycloak extends RxKeycloak {
    public static @NonNull Single<KeycloakToken> newSession(
            final KeycloakApiService service,
            final KeycloakConfig config,
            final String username,
            final String password
    ) {
        return buildRequest(config)
                .flatMap(authRequest -> service.newSession(
                        authRequest.configuration.tokenEndpoint.toString(),
                        authRequest.clientId,
                        username,
                        password,
                        KeycloakGrantType.PASSWORD
                ));
    }

    public static @NonNull Maybe<Boolean> createUser(
            final KeycloakApiService service,
            final KeycloakConfig config,
            final String username,
            final String email,
            final String password,
            final String confirmPassword
    ) {
        return buildRequest(config)

                // Step on sign in page
                .flatMap(authRequest ->
                    service.stepOnSignInPage(
                        Objects.requireNonNull(authRequest.configuration.authorizationEndpoint).toString(),
                        config.client,
                        config.redirectUri,
                        "code"
                ))

                // Extract signup link without session code from signin form
                .flatMap(r -> extractLinkFromElement(config, r, "//a", "href"))

                // Step on signup page
                .flatMap(service::stepOnSignUpPage)

                // Extract signup link with session code from signup form
                .flatMap(r -> extractLinkFromElement(config, r, "//form", "action"))

                // Create new user from the signup link with session code above
                .flatMap(r -> service.createUser(r, username, email, password, confirmPassword, ""))

                .flatMapMaybe(r -> {
                    var content = r.string();
                    if (content.contains("timed out")) {
                        return Maybe.error(new Error("Time out"));
                    }
                    if (content.contains("already exists")) {
                        return Maybe.just(false);
                    }
                    return Maybe.just(true);
                });
    }

    public static @NonNull Single<String> extractLinkFromElement(
            final KeycloakConfig config,
            final ResponseBody resp,
            final @NonNull String xPath,
            final @NonNull String attr
    ) {
        try {
            var redirectUri = Uri.parse(config.redirectUri);
            var baseUri = String.format("%s://%s", redirectUri.getScheme(), redirectUri.getHost());
            var document = Jsoup.parse(resp.string(), baseUri);
            var linkSelector = document.selectXpath(String.format("%s[@%s]", xPath, attr)).get(0);
            var signupUrl = linkSelector.attr(String.format("abs:%s", attr));
            return Single.just(signupUrl);
        } catch (IndexOutOfBoundsException e) {
            return Single.error(new Error("Cannot find link in request"));
        } catch (IOException e) {
            return Single.error(new Error("Invalid request"));
        } catch (Exception e) {
            Timber.w(e, "Unknown error occurred while extracting link");
            return Single.error(e);
        }
    }

    public static @NonNull Completable endSession(
            final KeycloakApiService service,
            final KeycloakConfig config
    ) {
        return buildRequest(config)
                .flatMapCompletable(authRequest -> service.endSession(
                        authRequest.configuration.tokenEndpoint.toString(),
                        authRequest.clientId,
                        MMKV.defaultMMKV().getString("refreshToken", "")
                ));
    }

    public static @NonNull Single<KeycloakToken> refreshSession(
            final KeycloakApiService service,
            final KeycloakConfig config
    ) {
        return buildRequest(config)
                .flatMap(authRequest -> service.refreshSession(
                        authRequest.configuration.tokenEndpoint.toString(),
                        authRequest.clientId,
                        MMKV.defaultMMKV().getString("refreshToken", ""),
                        KeycloakGrantType.REFRESH_TOKEN
                ));
    }
}
