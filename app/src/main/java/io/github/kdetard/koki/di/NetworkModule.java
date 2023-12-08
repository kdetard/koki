package io.github.kdetard.koki.di;

import android.content.Context;

import androidx.datastore.rxjava3.RxDataStore;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.keycloak.KeycloakApiService;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.github.kdetard.koki.network.MMKVCookieJar;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import timber.log.Timber;

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
            final Interceptor authenticator
    ) {
        return new OkHttpClient.Builder()
                .cache(cache)
                .cookieJar(cookieJar)
                .addInterceptor(logging)
                .addInterceptor(authenticator)
                .build();
    }

    @Provides
    @Singleton
    public static Interceptor provideAuthenticator(
            final JsonAdapter<KeycloakToken> keycloakTokenJsonAdapter,
            final RxDataStore<Settings> settings
    ) {
        return chain -> {
            var request = chain.request();

            if (!request.url().host().equals("uiot.ixxc.dev")) {
                return chain.proceed(request);
            }

            final var keycloakTokenJson = settings.data()
                    .map(Settings::getKeycloakTokenJson)
                    .map(json -> json.isEmpty() ? "{}" : json)
                    .blockingFirst();
            final var keycloakToken = keycloakTokenJsonAdapter.fromJson(keycloakTokenJson);


            if (keycloakToken != null) {
                Timber.d("Authenticator called with accessToken: %s", keycloakToken.accessToken);
                // Add new header to rejected request and retry it
                request = chain.request().newBuilder()
                        .header("Authorization", String.format("%s %s", keycloakToken.tokenType, keycloakToken.accessToken))
                        .build();
            }

            return chain.proceed(request);
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
