package io.github.kdetard.koki.di;

import com.tencent.mmkv.MMKV;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.db.MMKVCookieJar;
import io.github.kdetard.koki.network.KeycloakApiService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
    public static OkHttpClient provideOkHttpClient(
            final CookieJar cookieJar,
            final HttpLoggingInterceptor logging,
            final Authenticator authenticator
    ) {
        return new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(logging)
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

            /// TODO: Refresh your access token if expired

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
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
    }

    @Provides
    @Singleton
    public static KeycloakApiService provideKeycloakApiService(final Retrofit retrofit) {
        return retrofit.create(KeycloakApiService.class);
    }
}
