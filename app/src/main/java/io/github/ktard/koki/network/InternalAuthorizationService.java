package io.github.ktard.koki.network;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.LifecycleOwner;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.RedirectUriReceiverActivity;

import java.io.IOException;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.scopes.ActivityScoped;
import io.github.ktard.koki.di.RxKeycloak;
import io.github.ktard.koki.model.KeycloakGrantType;
import io.github.ktard.koki.model.KeycloakToken;
import io.github.ktard.koki.ui.EmptyActivity;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

@ActivityScoped
public class InternalAuthorizationService extends AuthorizationService {
    private final KeycloakApiService mKeycloakApi;
    private final Context mContext;
    private String mUsername;
    private String mPassword;
    private RxKeycloak.Config mConfig;
    private final JsonAdapter<KeycloakToken> tokenJsonAdapter;
    private static final String ERROR_FORMAT = "{\"error\": \"%s\"}";

    @Inject
    public InternalAuthorizationService(@ActivityContext @NonNull Context context, KeycloakApiService keycloakApi) {
        super(context);
        mContext = context;
        mKeycloakApi = keycloakApi;
        Moshi moshi = new Moshi.Builder().build();
        tokenJsonAdapter = moshi.adapter(KeycloakToken.class);
    }

    public void setCredentials(String username, String password) {
        mUsername = username;
        mPassword = password;
    }

    public void setClientConfig(RxKeycloak.Config config) {
        mConfig = config;
    }

    @Override
    public Intent getAuthorizationRequestIntent(
            @NonNull AuthorizationRequest request,
            @NonNull CustomTabsIntent customTabsIntent
    ) {
        var config = request.configuration;
        var tokenUrl = config.tokenEndpoint.toString();

        var call = mKeycloakApi.grantNewAccessToken(tokenUrl,
                mConfig.client, mUsername, mPassword, KeycloakGrantType.PASSWORD);

        var redirectIntent = new Intent(mContext, EmptyActivity.class);

        redirectIntent.putExtra("EMPTY", true);

        var result = call.blockingFirst();
        String errorJson = null;

        if (result.isError()) {
            assert result.error() != null;
            errorJson = String.format(ERROR_FORMAT, result.error());
        }
        else {
            assert result.response() != null;
            if (!result.response().isSuccessful()){
                errorJson = String.format(ERROR_FORMAT, result.response().errorBody());
            }
        }

        if (errorJson == null) {
            var resp = result.response();
            redirectIntent.setData(Uri.parse(mConfig.redirectUri));
            redirectIntent.putExtra(AuthorizationResponse.EXTRA_RESPONSE, tokenJsonAdapter.toJson(resp.body()));
        }
        else {
            redirectIntent.putExtra(AuthorizationException.EXTRA_EXCEPTION, errorJson);
        }

        return redirectIntent;
    }
}
