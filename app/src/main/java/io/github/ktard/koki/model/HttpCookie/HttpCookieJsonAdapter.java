package io.github.ktard.koki.model.HttpCookie;

import com.google.gson.Gson;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.net.HttpCookie;

public final class HttpCookieJsonAdapter {
    @FromJson
    HttpCookie httpCookieFromJson(final HttpCookieJson cookieJson) {
        var cookie = new HttpCookie(cookieJson.name, cookieJson.value);
        cookie.setComment(cookieJson.comment);
        cookie.setCommentURL(cookieJson.commentURL);
        cookie.setDiscard(cookieJson.discard);
        cookie.setDomain(cookieJson.domain);
        cookie.setMaxAge(cookieJson.maxAge);
        cookie.setPath(cookieJson.path);
        cookie.setPortlist(cookieJson.portlist);
        cookie.setSecure(cookieJson.secure);
        cookie.setVersion(cookieJson.version);
        cookie.setHttpOnly(cookieJson.httpOnly);
        return cookie;
    }

    @ToJson
    HttpCookieJson httpCookieToJson(final HttpCookie cookie) {
        return new HttpCookieJson(cookie);
    }
}
