package io.github.kdetard.koki.keycloak;

import android.net.Uri;

import com.tencent.mmkv.MMKV;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Objects;

import io.github.kdetard.koki.feature.auth.SignUpResult;
import io.github.kdetard.koki.keycloak.models.KeycloakConfig;
import io.github.kdetard.koki.keycloak.models.KeycloakGrantType;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
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

    public static @NonNull Single<SignUpResult> createUser(
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

                .doOnSuccess(r -> Timber.d("Sign up link (no session code): %s", r))

                // Step on signup page
                .flatMap(service::stepOnSignUpPage)

                // Extract signup link with session code from sign up form
                .flatMap(r -> extractLinkFromElement(config, r, "//form", "action"))

                .doOnSuccess(r -> Timber.d("Sign up link (with session code): %s", r))

                // Create new user from the signup link with session code above
                .flatMap(r -> service.createUser(r, username, email, password, confirmPassword, ""))

                .onErrorResumeNext(e -> {
                    Timber.w(e, "Error occurred while creating user");
                    return Single.error(new Error(SignUpResult.UNKNOWN.toString()));
                })

                // check sign up result
                .flatMap(r -> {
                    var result = SignUpResult.UNKNOWN;

                    if (r.body() == null) {
                        result = SignUpResult.NULL;
                    }
                    else {
                        final var body = r.body().string();
                        if (body.isEmpty()) {
                            result = SignUpResult.EMPTY;
                        }
                        if (body.contains("Invalid")) {
                            result = SignUpResult.INVALID;
                        }
                        if (body.contains("specify")) {
                            result = SignUpResult.SPECIFY;
                        }
                        if (body.contains("timed out")) {
                            result = SignUpResult.TIMEOUT;
                        }
                        if (body.contains("Username already")) {
                            result = SignUpResult.USERNAME_EXISTS;
                        }
                        if (body.contains("Email already")) {
                            result = SignUpResult.EMAIL_EXISTS;
                        }
                        if (body.contains("Welcome to Keycloak")) {
                            result = SignUpResult.SUCCESS;
                        }
                    }

                    return Single.just(result);
                });
    }

    public static @NonNull Single<String> extractLinkFromElement(
            final KeycloakConfig config,
            final ResponseBody resp,
            final @NonNull String xPath,
            final @NonNull String attr
    ) {
        try {
            final var redirectUri = Uri.parse(config.authServerUrl);
            final var baseUri = String.format("%s://%s", redirectUri.getScheme(), redirectUri.getHost());
            final var document = Jsoup.parse(resp.string(), baseUri);
            final var linkSelector = document.selectXpath(String.format("%s[@%s]", xPath, attr)).get(0);
            final var signupUrl = linkSelector.attr(String.format("abs:%s", attr));
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
