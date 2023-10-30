package io.github.ktard.koki.db;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tencent.mmkv.MMKV;

import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.ktard.koki.model.HttpCookie.HttpCookieJsonAdapter;

public class MMKVCookieStore implements CookieStore {
    private final MMKV store;
    private final JsonAdapter<HttpCookie> cookieJsonAdapter;

    public MMKVCookieStore(String name) {
        super();
        store = MMKV.mmkvWithID(name, MMKV.MULTI_PROCESS_MODE);
        Moshi moshi = new Moshi.Builder()
                .add(new HttpCookieJsonAdapter())
                .build();
        cookieJsonAdapter = moshi.adapter(HttpCookie.class).nullSafe();
    }

    private HttpCookie fromJson(String cookie) {
        try {
            return cookieJsonAdapter.fromJson(cookie);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        var cookies = store.getStringSet(uri.toString(), Collections.emptySet());
        cookies.add(cookieJsonAdapter.toJson(cookie));
        store.encode(uri.toString(), cookies);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return store.getStringSet(uri.toString(), Collections.emptySet()).stream()
                .map(this::fromJson)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<HttpCookie> getCookies() {
        var keys = store.allKeys();

        if (keys == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(keys)
                .map(k -> store.getString(k, ""))
                .map(this::fromJson)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<URI> getURIs() {
        var keys = store.allKeys();

        if (keys == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(keys)
                .map(URI::create)
                .collect(Collectors.toList());
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        var cookies = get(uri);

        if (cookies.contains(cookie)) {
            cookies.remove(cookie);

            var finalCookies = cookies.stream()
                    .map(cookieJsonAdapter::toJson)
                    .collect(Collectors.toSet());

            store.encode(uri.toString(), finalCookies);

            return true;
        }

        return false;
    }

    @Override
    public boolean removeAll() {
        var keys = store.allKeys();
        if (keys != null && keys.length > 0) {
            store.clearAll();
            return true;
        }
        return false;
    }
}
