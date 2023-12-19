package io.github.kdetard.koki.keycloak.models;

import com.squareup.moshi.Json;

// from https://github.com/maslick/keycloak-android-native/blob/99cedc83158bce8e9f8b294f99d6c6425dd34fd9/app/src/main/java/io/maslick/keycloaker/di/KoinConfig.kt#L80-L91
public record KeycloakToken(
    @Json(name = "access_token")               String accessToken,
    @Json(name = "expires_in")                 long expiresIn,
    @Json(name = "refresh_expires_in")         long refreshExpiresIn,
    @Json(name = "refresh_token")              String refreshToken,
    @Json(name = "token_type")                 String tokenType,
    @Json(name = "id_token")                   String idToken,
    @Json(name = "not-before-policy")          long notBeforePolicy,
    @Json(name = "session_state")              String sessionState,
    @Json(name = "token_expiration_date")      String tokenExpirationDate,
    @Json(name = "refresh_expiration_date")    String refreshTokenExpirationDate,
    String scope
) { }
