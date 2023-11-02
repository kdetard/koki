package io.github.kdetard.koki.db;

import androidx.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tencent.mmkv.MMKV;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.kdetard.koki.model.Cookie.CookieJsonAdapter;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class MMKVCookieJar implements CookieJar {
    private final MMKV store;
    private final JsonAdapter<Cookie> cookieJsonAdapter;

    public MMKVCookieJar(String name) {
        super();
        store = MMKV.mmkvWithID(name, MMKV.MULTI_PROCESS_MODE);
        final Moshi moshi = new Moshi.Builder()
                .add(new CookieJsonAdapter())
                .build();
        cookieJsonAdapter = moshi.adapter(Cookie.class).nullSafe();
    }

    private Cookie tryParse(final String cookieJsonString) {
        try {
            return cookieJsonAdapter.fromJson(cookieJsonString);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> list) {
        final String url = httpUrl.host();
        final HashSet<String> cookies = new HashSet<>(store.getStringSet(url, new HashSet<>()));

        cookies.addAll(
                list.stream()
                        .map(cookieJsonAdapter::toJson)
                        .collect(Collectors.toList()));

        store.encode(url, cookies);
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {
        final String url = httpUrl.host();
        final HashSet<String> cookies = new HashSet<>(store.getStringSet(url, new HashSet<>()));

        return cookies.stream()
                .map(c -> {
                    final Cookie cookie = tryParse(c);
                    if (cookie == null || cookie.expiresAt() < System.currentTimeMillis()) {
                        return null;
                    }
                    return cookie;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
