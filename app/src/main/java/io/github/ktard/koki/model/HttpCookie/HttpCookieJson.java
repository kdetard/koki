package io.github.ktard.koki.model.HttpCookie;

import java.net.HttpCookie;

public class HttpCookieJson {
    String comment;
    String commentURL;
    boolean discard;
    String domain;
    long maxAge;
    String name;
    String path;
    String portlist;
    boolean secure;
    String value;
    int version;
    boolean httpOnly;

    public HttpCookieJson(HttpCookie cookie) {
        comment = cookie.getComment();
        commentURL = cookie.getCommentURL();
        discard = cookie.getDiscard();
        domain = cookie.getDomain();
        maxAge = cookie.getMaxAge();
        name = cookie.getName();
        path = cookie.getPath();
        portlist = cookie.getPortlist();
        secure = cookie.getSecure();
        value = cookie.getValue();
        version = cookie.getVersion();
        httpOnly = cookie.isHttpOnly();
    }
}
