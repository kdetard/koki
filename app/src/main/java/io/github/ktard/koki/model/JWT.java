package io.github.ktard.koki.model;

import android.util.Base64;

public class JWT {
    public final String header;
    public final String body;
    public final String signature;

    public JWT(final String accessToken) {
        final String[] tokenParts = accessToken.split("\\.");
        this.header = new String(Base64.decode(tokenParts[0], Base64.DEFAULT));
        this.body = new String(Base64.decode(tokenParts[1], Base64.DEFAULT));
        this.signature = tokenParts[2];
    }
}
