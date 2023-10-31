package io.github.ktard.koki.model.Keycloak;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.GrantTypeValues;
import net.openid.appauth.RegistrationRequest;

/**
 * Inherited from {@link GrantTypeValues}
 * The grant type values defined by the [OAuth2 spec](https://tools.ietf.org/html/rfc6749), and
 * used in {@link AuthorizationRequest authorization} and
 * {@link RegistrationRequest dynamic client registration} requests.
 */
public class KeycloakGrantType {
    /**
     * The grant type used for exchanging an authorization code for one or more tokens.
     *
     * @see "The OAuth 2.0 Authorization Framework (RFC 6749), Section 4.1.3
     * <https://tools.ietf.org/html/rfc6749#section-4.1.3>"
     */
    public static final String AUTHORIZATION_CODE = "authorization_code";

    /**
     * The grant type used when obtaining an access token.
     *
     * @see "The OAuth 2.0 Authorization Framework (RFC 6749), Section 4.2
     * <https://tools.ietf.org/html/rfc6749#section-4.2>"
     */
    public static final String IMPLICIT = "implicit";

    /**
     * The grant type used when obtaining an access token via password.
     *
     * @see "The OAuth 2.0 Authorization Framework (RFC 6749), Section 6
     * <https://tools.ietf.org/html/rfc6749#section-4.3>"
     */
    public static final String PASSWORD = "password";

    /**
     * The grant type used when exchanging a refresh token for a new token.
     *
     * @see "The OAuth 2.0 Authorization Framework (RFC 6749), Section 6
     * <https://tools.ietf.org/html/rfc6749#section-6>"
     */
    public static final String REFRESH_TOKEN = "refresh_token";
}
