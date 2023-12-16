package io.github.kdetard.koki.di;

import android.content.Context;

import androidx.datastore.rxjava3.RxDataStore;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.util.Objects;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.BuildConfig;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.aqicn.AqicnService;
import io.github.kdetard.koki.keycloak.KeycloakApiService;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.github.kdetard.koki.network.CacheInterceptor;
import io.github.kdetard.koki.network.MMKVCookieJar;
import io.github.kdetard.koki.network.NetworkUtils;
import io.github.kdetard.koki.network.StrictAuthenticator;
import io.github.kdetard.koki.openmeteo.OpenMeteoService;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
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
                .setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
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
            final Authenticator laxAuthenticator,
            final @StrictAuthenticator Interceptor strictAuthenticator,
            final @CacheInterceptor Interceptor cacheInterceptor
    ) {
        return new OkHttpClient.Builder()
                .cache(cache)
                .cookieJar(cookieJar)
                .addInterceptor(logging)
                .authenticator(laxAuthenticator)
                .addInterceptor(strictAuthenticator)
                .addInterceptor(cacheInterceptor)
                .build();
    }

    @Provides
    @Singleton
    public static Authenticator provideLaxAuthenticator(
            final JsonAdapter<KeycloakToken> keycloakTokenJsonAdapter,
            final RxDataStore<Settings> settings
    ) {
        return (route, response) ->
            NetworkUtils.commonAuthenticator(
                keycloakTokenJsonAdapter,
                settings,
                route == null ? null : route.address().url().host(),
                response.request(),
                response
            );
    }

    @Provides
    @Singleton
    @StrictAuthenticator
    public static Interceptor provideStrictAuthenticator(
            final JsonAdapter<KeycloakToken> keycloakTokenJsonAdapter,
            final RxDataStore<Settings> settings
    ) {
        return chain -> chain.proceed(
            Objects.requireNonNull(NetworkUtils.commonAuthenticator(
                keycloakTokenJsonAdapter,
                settings,
                chain.request().url().host(),
                chain.request(),
                null
            ))
        );
    }

    @Provides
    @Singleton
    @CacheInterceptor
    public static Interceptor provideCacheInterceptor(final @ApplicationContext Context context) {
        return chain -> {
            // Get the request from the chain.
            var request = chain.request();

            /*
             *  Leveraging the advantage of using Kotlin,
             *  we initialize the request and change its header depending on whether
             *  the device is connected to Internet or not.
             */
            if (NetworkUtils.hasNetwork(context)) {
                /*
                 *  If there is Internet, get the cache that was stored 5 seconds ago.
                 *  If the cache is older than 5 seconds, then discard it,
                 *  and indicate an error in fetching the response.
                 *  The 'max-age' attribute is responsible for this behavior.
                 */
                request = request.newBuilder().header("Cache-Control", "public, max-age=" + 5).build();
            } else
            /*
             *  If there is no Internet, get the cache that was stored 7 days ago.
             *  If the cache is older than 7 days, then discard it,
             *  and indicate an error in fetching the response.
             *  The 'max-stale' attribute is responsible for this behavior.
             *  The 'only-if-cached' attribute indicates to not retrieve new data; fetch the cache only instead.
             */ {
                request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
            }
            // End of if-else statement

            // Add the modified request to the chain.
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

    @Provides
    @Singleton
    public static OpenMeteoService provideOpenMeteoService(final Retrofit retrofit) {
        return retrofit.create(OpenMeteoService.class);
    }

    @Provides
    @Singleton
    public static AqicnService provideAqicnService(final Retrofit retrofit) {
        return retrofit.create(AqicnService.class);
    }
}
