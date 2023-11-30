package io.github.kdetard.koki.keycloak;

import android.net.Uri;

import com.tencent.mmkv.MMKV;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Objects;

import io.github.kdetard.koki.keycloak.models.KeycloakConfig;
import io.github.kdetard.koki.keycloak.models.KeycloakGrantType;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

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
                        // uiot.ixxc.dev/auth/..../auth
                        Objects.requireNonNull(authRequest.configuration.authorizationEndpoint).toString(),
                        config.client,
                        config.authServerUrl,
                        "code"
                ))

                // Extract signup link without session code from sign in form
                .flatMap(r -> extractLinkFromElement(config, r, "//a", "href"))

                // Step on signup page
                .flatMap(service::stepOnSignUpPage)

                // Extract signup link with session code from sign up form
                .flatMap(r -> extractLinkFromElement(config, r, "//form", "action"))

                // Create new user from the signup link with session code above
                .flatMap(r -> service.createUser(r, username, email, password, confirmPassword, ""))

                // TODO: try to login instead of checking sign up page.
                .flatMapMaybe(r -> {
                    final Response raw = r.raw();
                    try {
                        if (!raw.request().url().url().getPath().contains("/manager/")) {
                            return Maybe.error(new Error("Not redirected to manager"));
                        }
                    } finally {
                        raw.close();
                    }

                    final ResponseBody body = r.body();
                    if (body == null || body.string().isEmpty()) {
                        return Maybe.error(new Error("Empty response"));
                    }

                    final String content = body.string();
                    if (content.contains("Invalid")) {
                        return Maybe.error(new Error("Some fields are invalid"));
                    }
                    if (content.contains("specify")) {
                        return Maybe.error(new Error("Some fields are empty"));
                    }
                    if (content.contains("timed out")) {
                        return Maybe.error(new Error("Time out"));
                    }
                    if (content.contains("exists")) {
                        return Maybe.error(new Error("User already exists"));
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
            final Uri redirectUri = Uri.parse(config.authServerUrl);
            final String baseUri = String.format("%s://%s", redirectUri.getScheme(), redirectUri.getHost());
            final Document document = Jsoup.parse(resp.string(), baseUri);
            final Element linkSelector = document.selectXpath(String.format("%s[@%s]", xPath, attr)).get(0);
            final String signupUrl = linkSelector.attr(String.format("abs:%s", attr));
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
