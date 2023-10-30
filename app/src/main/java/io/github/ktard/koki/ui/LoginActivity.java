package io.github.ktard.koki.ui;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxTextView;

import net.openid.appauth.AuthorizationService;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.ktard.koki.R;
import io.github.ktard.koki.di.RxKeycloak;
import io.github.ktard.koki.model.RxActivity;
import io.github.ktard.koki.network.InternalAuthorizationService;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private static final int AUTH_REQUEST_CODE  = 2001;

    // Stream subject for onActivityResult method:
    private final PublishSubject<RxActivity.ActivityResult> mOnActivityResult = PublishSubject.create();

    // Internal cached instance of the Keycloak config:
    private RxKeycloak.Config mKeycloakConfig;

    private String mUsername;
    private String mPassword;

    @Inject
    InternalAuthorizationService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply View:
        setContentView(R.layout.activity_login);

        mKeycloakConfig = RxKeycloak.Config.getDefaultConfig(this);

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.loginBtn))

                //Clear Results
                .doOnNext(v -> {
                    mUsername = ((TextInputEditText)findViewById(R.id.usernameTxt)).getText().toString();
                    mPassword = ((TextInputEditText)findViewById(R.id.passwordTxt)).getText().toString();
                    authService.setCredentials(mUsername, mPassword);
                    authService.setClientConfig(mKeycloakConfig);
                    ((TextView)findViewById(R.id.keycloakResponse)).setText("");
                })

                //Start Auth:
                .flatMapMaybe(v ->
                        RxKeycloak.service(authService)
                                .flatMapMaybe(authService ->

                                        //Build config object for Keycloak based on form field values:
                                        Single.just(mKeycloakConfig)

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