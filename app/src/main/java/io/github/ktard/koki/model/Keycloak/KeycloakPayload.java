package io.github.ktard.koki.model.Keycloak;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public final class KeycloakPayload implements Serializable {

    public static class RealmAccess implements Serializable {
        public String[] roles;
    }

    public static class Account implements Serializable {
        public String[] roles;
    }

    public static class ResourceAccess implements Serializable {
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

    public static KeycloakPayload fromJson(final String json) {
        return new Gson().fromJson(json, KeycloakPayload.class);
    }
}
