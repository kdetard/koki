package io.github.kdetard.koki.di;

import android.content.Context;

import com.squareup.moshi.Moshi;
import com.tencent.mmkv.MMKV;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.keycloak.KeycloakApiService;
import io.github.kdetard.koki.network.MMKVCookieJar;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public abstract class NetworkModule {
    public static final String BASE_URL = "https://uiot.ixxc.dev";
    public static final String COOKIE_STORE_NAME = "kookies"; // get it? ;D

    @Provides
    @Singleton
    public static CookieJar provideCookieJar() {
        return new MMKVCookieJar(COOKIE_STORE_NAME);
    }

    @Provides
    @Singleton
    public static HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        return new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Provides
    @Singleton
    public static Cache provideCache(final @ApplicationContext Context context) {
        return new Cache(new File(context.getCacheDir(), "http_cache"), 50L * 1024L * 1024L); // 10 MiB
    }

    @Provides
    @Singleton
    public static OkHttpClient provideOkHttpClient(
            final Cache cache,
            final CookieJar cookieJar,
            final HttpLoggingInterceptor logging,
            final Authenticator authenticator
    ) {
        return new OkHttpClient.Builder()
                .cache(cache)
                .cookieJar(cookieJar)
                .addInterceptor(logging)
                .authenticator(authenticator)
                .build();
    }

    @Provides
    @Singleton
    public static Authenticator provideAuthenticator() {
        final var kv = MMKV.defaultMMKV(MMKV.MULTI_PROCESS_MODE, null);

        return (route, response) -> {
            final var accessToken = kv.getString("accessToken", "");

            if (accessToken.isEmpty()) {
                return response.request();
            }

            /// TODO: Refresh your access token if expired

            // Add new header to rejected request and retry it
            return response.request().newBuilder()
                    .header("Authorization", String.format("Bearer %s", accessToken))
                    .build();
        };
    }

    @Provides
    @Singleton
    public static Retrofit provideRetrofit(final OkHttpClient okHttpClient, final Moshi moshi) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
    }

    @Provides
    @Singleton
    public static KeycloakApiService provideKeycloakApiService(final Retrofit retrofit) {
        return retrofit.create(KeycloakApiService.class);
    }

    @Provides
    @Singleton
    public static OpenRemoteService provideOpenRemoteService(final Retrofit retrofit) {
        return retrofit.create(OpenRemoteService.class);
    }
}
