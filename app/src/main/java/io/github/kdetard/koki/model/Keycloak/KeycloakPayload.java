package io.github.kdetard.koki.model.Keycloak;

import com.squareup.moshi.Json;

import java.io.Serializable;

public final class KeycloakPayload implements Serializable {

    public static class RealmAccess implements Serializable {
        public String[] roles;
    }

    public static class Account implements Serializable {
        public String[] roles;
    }

    public static class ResourceAccess {
        public Account account;
    }

    public String acr;

    @Json(name = "allowed-origins")
    public String[] allowedOrigins;

    public String aud;

    @Json(name = "auth_time")
    public int authTime;

    public String azp;

    public String email;

    @Json(name = "email_verified")
    public boolean emailVerified;

    public int exp;

    @Json(name = "family_name")
    public String familyName;

    @Json(name = "given_name")
    public String givenName;

    public int iat;

    public String iss;

    public String jti;

    public String name;

    @Json(name = "preferred_username")
    public String preferredUsername;

    @Json(name = "realm_access")
    public RealmAccess realmAccess;

    @Json(name = "resource_access")
    public ResourceAccess resourceAccess;

    public String scope;

    @Json(name = "session_state")
    public String sessionState;

    public String sub;

    public String typ;
}
