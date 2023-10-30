package io.github.ktard.koki.ui;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.github.ktard.koki.R;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;

import net.openid.appauth.AuthorizationService;

import io.github.ktard.koki.model.RxActivity;
import io.github.ktard.koki.di.RxKeycloak;

public class CustomTabLoginActivity extends AppCompatActivity {
    // Arbitrary Request Code for capturing messages sent to this activity:
    private static final int AUTH_REQUEST_CODE  = 2001;

    // Stream subject for onActivityResult method:
    private final PublishSubject<RxActivity.ActivityResult> mOnActivityResult = PublishSubject.create();

    // Internal cached instance of the Keycloak config:
    private RxKeycloak.Config mKeycloakConfig;

    /**
     * @return Generate and return a default keycloak config object drawing values
     * from resource file: keycloak-config.
     */
    private RxKeycloak.Config getDefaultConfig() {
        if(mKeycloakConfig == null) {
            mKeycloakConfig = RxKeycloak.Config.getDefaultConfig(this);
        }
        return mKeycloakConfig;
    }

    /**
     * Apply the supplied config to the UI, populating all form fields.
     */
    private void applyConfigToUI(final RxKeycloak.Config config) {
        ((TextInputEditText)findViewById(R.id.authserver)).setText(config.authServerUrl);
        ((TextInputEditText)findViewById(R.id.realm)).setText(config.realm);
        ((TextInputEditText)findViewById(R.id.clientid)).setText(config.client);
    }

    /**
     * @return A Keycloak config object created from merging the default config
     * with the current values displayed in the UI.
     */
    private RxKeycloak.Config extractConfigFromUI(final RxKeycloak.Config defaultConfig) {
        return new RxKeycloak.Config(
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
        setContentView(R.layout.activity_custom_tab_login);

        //Populate the UI from cached/default config data:
        Single.just(getDefaultConfig())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(this::applyConfigToUI);

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.customTabLoginBtn))

                //Clear Results
                .doOnNext(v -> ((TextView)findViewById(R.id.keycloakResponse)).setText(""))

                //Start Auth:
                .flatMapMaybe(v ->
                        RxKeycloak.service(new AuthorizationService(this))
                                .flatMapMaybe(authService ->

                                        //Build config object for Keycloak based on form field values:
                                        Single.just(extractConfigFromUI(getDefaultConfig()))

                                                //Make auth request to keycloak server:
                                                .flatMap(config -> RxKeycloak.auth(this, authService, config, AUTH_REQUEST_CODE))

                                                //Wait for auth result on Activity:
                                                .flatMapMaybe(config -> RxKeycloak.authResult(mOnActivityResult, AUTH_REQUEST_CODE))

                                                //Exchange code for accessToken
                                                .flatMapSingle(response -> RxKeycloak.exchangeTokens(authService, response))

                                ).firstOrError().onErrorComplete()
                )

                //Populate result with the parsed token body:
                .doOnNext(r -> {
                    final RxKeycloak.JWT jwt = new RxKeycloak.JWT(r.accessToken);
                    ((TextView)findViewById(R.id.keycloakResponse)).setText(jwt.body);
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