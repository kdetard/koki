package io.github.ktard.koki.model.Cookie;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import okhttp3.Cookie;

public class CookieJsonAdapter {
    @FromJson
    Cookie cookieFromJson(CookieJson cookieJson) {
        var cookieBuilder = new Cookie.Builder()
                .name(cookieJson.name)
                .value(cookieJson.value)
                .expiresAt(cookieJson.expiresAt)
                .domain(cookieJson.domain)
                .path(cookieJson.path);

        if (cookieJson.secure) {
            cookieBuilder.secure();
        }

        if (cookieJson.httpOnly) {
            cookieBuilder.httpOnly();
        }

        if (cookieJson.hostOnly) {
            cookieBuilder.hostOnlyDomain(cookieJson.domain);
        }

        return cookieBuilder.build();
    }

    @ToJson
    CookieJson cookieToJson(Cookie cookie) {
        var cookieJson = new CookieJson();
        cookieJson.name = cookie.name();
        cookieJson.value = cookie.value();
        cookieJson.expiresAt = cookie.expiresAt();
        cookieJson.domain = cookie.domain();
        cookieJson.path = cookie.path();
        cookieJson.secure = cookie.secure();
        cookieJson.httpOnly = cookie.httpOnly();
        cookieJson.persistent = cookie.persistent();
        cookieJson.hostOnly = cookie.hostOnly();
        return cookieJson;
    }
}
