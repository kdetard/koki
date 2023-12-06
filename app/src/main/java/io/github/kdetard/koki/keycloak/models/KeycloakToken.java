package io.github.kdetard.koki.keycloak.models;

import com.squareup.moshi.Json;

// from https://github.com/maslick/keycloak-android-native/blob/99cedc83158bce8e9f8b294f99d6c6425dd34fd9/app/src/main/java/io/maslick/keycloaker/di/KoinConfig.kt#L80-L91
public class KeycloakToken {
        @Json(name = "access_token")               public String accessToken;
        @Json(name = "expires_in")                 public String expiresIn;
        @Json(name = "refresh_expires_in")         public String refreshExpiresIn;
        @Json(name = "refresh_token")              public String refreshToken;
        @Json(name = "token_type")                 public String tokenType;
        @Json(name = "id_token")                   public String idToken;
        @Json(name = "not-before-policy")          public String notBeforePolicy;
        @Json(name = "session_state")              public String sessionState;
        @Json(name = "token_expiration_date")      public String tokenExpirationDate;
        @Json(name = "refresh_expiration_date")    public String refreshTokenExpirationDate;
}
