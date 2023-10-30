package io.github.ktard.koki.di;

import android.content.Context;

import com.tencent.mmkv.MMKV;

import net.gotev.cookiestore.okhttp.JavaNetCookieJar;

import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.github.ktard.koki.db.MMKVCookieStore;
import io.github.ktard.koki.network.KeycloakApiService;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    public static final String BASE_URL = "https://uiot.ixxc.dev";
    public static final String COOKIE_STORE_NAME = "kookies"; // get it? ;D

    @Provides
    @Singleton
    public static CookieManager provideCookieManager() {
        return new CookieManager(
                new MMKVCookieStore(COOKIE_STORE_NAME),
                CookiePolicy.ACCEPT_ORIGINAL_SERVER
        );
    }

    @Provides
    @Singleton
    public static OkHttpClient provideOkHttpClient(/*final CookieManager cookieManager, */Authenticator authenticator) {
        // .cookieJar(new JavaNetCookieJar(cookieManager))
        return new OkHttpClient.Builder()
                .authenticator(authenticator)
                .build();
    }

    @Provides
    @Singleton
    public static Authenticator provideAuthenticator() {
        MMKV kv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null);

        return (route, response) -> {
            String accessToken = kv.getString("accessToken", "");

            if (accessToken.isEmpty()) {
                return response.request();
            }

            // TODO: Refresh your access token if expired

            // Add new header to rejected request and retry it
            return response.request().newBuilder()
                    .header("Authorization", String.format("Bearer %s", accessToken))
                    .build();
        };
    }

    @Provides
    @Singleton
    public static Retrofit provideRetrofit(final OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public static KeycloakApiService provideKeycloakApiService(final Retrofit retrofit) {
        return retrofit.create(KeycloakApiService.class);
    }
}
