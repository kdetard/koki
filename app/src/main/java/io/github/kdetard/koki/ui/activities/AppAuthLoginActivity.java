package io.github.kdetard.koki.ui.activities;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.model.JWT;
import io.github.kdetard.koki.model.Keycloak.KeycloakConfig;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;

import io.github.kdetard.koki.model.RxActivity;
import io.github.kdetard.koki.di.RxAppAuthKeycloak;

public class AppAuthLoginActivity extends AppCompatActivity {
    // Arbitrary Request Code for capturing messages sent to this activity:
    private static final int AUTH_REQUEST_CODE  = 2001;

    // Stream subject for onActivityResult method:
    private final PublishSubject<RxActivity.ActivityResult> mOnActivityResult = PublishSubject.create();

    // Internal cached instance of the Keycloak config:
    private KeycloakConfig mKeycloakConfig;

    /**
     * @return Generate and return a default keycloak config object drawing values
     * from resource file: keycloak-config.
     */
    private KeycloakConfig getDefaultConfig() {
        if(mKeycloakConfig == null) {
            mKeycloakConfig = KeycloakConfig.getDefaultConfig(this);
        }
        return mKeycloakConfig;
    }

    /**
     * Apply the supplied config to the UI, populating all form fields.
     */
    private void applyConfigToUI(final KeycloakConfig config) {
        ((TextInputEditText)findViewById(R.id.authserver)).setText(config.authServerUrl);
        ((TextInputEditText)findViewById(R.id.realm)).setText(config.realm);
        ((TextInputEditText)findViewById(R.id.clientid)).setText(config.client);
    }

    /**
     * @return A Keycloak config object created from merging the default config
     * with the current values displayed in the UI.
     */
    private KeycloakConfig extractConfigFromUI(final KeycloakConfig defaultConfig) {
        return new KeycloakConfig(
                ((TextInputEditText)findViewById(R.id.authserver)).getText().toString(),
                ((TextInputEditText)findViewById(R.id.realm)).getText().toString(),
                ((TextInputEditText)findViewById(R.id.clientid)).getText().toString(),
                defaultConfig.redirectUri
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply View:
        setContentView(R.layout.activity_app_auth_login);

        //Populate the UI from cached/default config data:
        Single.just(getDefaultConfig())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(this::applyConfigToUI);

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.appAuthLogin_loginBtn))

                //Clear Results
                .doOnNext(v -> ((TextView)findViewById(R.id.appAuthLogin_keycloakResponse)).setText(""))

                //Start Auth:
                .flatMapMaybe(v ->
                        RxAppAuthKeycloak.service(this)
                                .flatMapMaybe(authService ->

                                        //Build config object for Keycloak based on form field values:
                                        Single.just(extractConfigFromUI(getDefaultConfig()))

                                                //Make auth request to keycloak server:
                                                .flatMap(config -> RxAppAuthKeycloak.auth(this, authService, config, AUTH_REQUEST_CODE))

                                                //Wait for auth result on Activity:
                                                .flatMapMaybe(config -> RxAppAuthKeycloak.authResult(mOnActivityResult, AUTH_REQUEST_CODE))

                                                //Exchange code for accessToken
                                                .flatMapSingle(response -> RxAppAuthKeycloak.exchangeTokens(authService, response))

                                ).firstOrError().onErrorComplete()
                )

                //Populate result with the parsed token body:
                .doOnNext(r -> {
                    final JWT jwt = new JWT(r.accessToken);
                    ((TextView)findViewById(R.id.appAuthLogin_keycloakResponse)).setText(jwt.body);
                })

                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))

                .subscribe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mOnActivityResult.onNext(new RxActivity.ActivityResult(requestCode, resultCode, data));
    }
}