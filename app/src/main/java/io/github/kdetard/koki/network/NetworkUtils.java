package io.github.kdetard.koki.network;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.rxjava3.RxDataStore;

import com.squareup.moshi.JsonAdapter;

import java.io.IOException;

import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class NetworkUtils {
    public static boolean hasNetwork(@NonNull Context context) {
        var isConnected = false; // Initial Value
        var connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        var activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected())
            isConnected = true;
        return isConnected;
    }

    @Nullable
    public static Request commonAuthenticator(
        @NonNull JsonAdapter<KeycloakToken> keycloakTokenJsonAdapter,
        @NonNull RxDataStore<Settings> settings,
        @Nullable String host,
        @NonNull Request request,
        @Nullable Response response
    ) throws IOException {
        if (host == null || !host.equals("uiot.ixxc.dev") || request.header("Authorization") != null)
            return request;

        if (responseCount(response) >= 3) {
            return null; // If we've failed 3 times, give up. - in real life, never give up!!
        }

        final var keycloakTokenJson = settings.data()
                .map(Settings::getKeycloakTokenJson)
                .map(json -> json.isEmpty() ? "{}" : json)
                .blockingFirst();

        final var keycloakToken = keycloakTokenJsonAdapter.fromJson(keycloakTokenJson);

        if (keycloakToken != null) {
            Timber.d("Authenticator called with accessToken: %s", keycloakToken.accessToken);
            // Add new header to rejected request and retry it
            return request.newBuilder()
                    .header("Authorization", String.format("%s %s", keycloakToken.tokenType, keycloakToken.accessToken))
                    .build();
        }

        return request;
    }

    private static int responseCount(Response response) {
        if (response == null) return 0;
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    public static void handleError(@NonNull Context context, @NonNull Throwable throwable) {
        Timber.e(throwable, "Network Error");
        if (throwable instanceof IOException) {
            if (!hasNetwork(context)) {
                // No Internet Connection
                Timber.e("No Internet Connection");
            } else {
                // Other IOException
                Timber.e("Other IOException");
            }
        } else {
            // Other Throwable
            Timber.e("Other Throwable");
        }
    }
}
