package io.github.kdetard.koki.ui;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.model.JWT;
import io.github.kdetard.koki.model.Keycloak.KeycloakConfig;
import io.github.kdetard.koki.di.RxRestKeycloak;
import io.github.kdetard.koki.network.KeycloakApiService;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class RestLoginActivity extends AppCompatActivity {
    // Internal cached instance of the Keycloak config:
    private KeycloakConfig mKeycloakConfig;

    private String mUsername;

    private String mPassword;

    @Inject
    KeycloakApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply View:
        setContentView(R.layout.activity_rest_login);

        mKeycloakConfig = KeycloakConfig.getDefaultConfig(this);

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.restLogin_loginBtn))

                .throttleFirst(500, TimeUnit.MILLISECONDS)

                //Clear Results
                .doOnNext(v -> {
                    mUsername = ((TextInputEditText)findViewById(R.id.restLogin_usernameTxt)).getText().toString();
                    mPassword = ((TextInputEditText)findViewById(R.id.restLogin_passwordTxt)).getText().toString();
                    ((TextView)findViewById(R.id.appAuthLogin_keycloakResponse)).setText("");
                })

                .subscribeOn(AndroidSchedulers.mainThread())

                //Start Auth:
                .flatMapSingle(v ->
                        RxRestKeycloak.newSession(apiService, mKeycloakConfig, mUsername, mPassword)
                                .subscribeOn(Schedulers.io()))

                .observeOn(AndroidSchedulers.mainThread())

                .doOnError(throwable ->
                        ((TextView)findViewById(R.id.appAuthLogin_keycloakResponse)).setText(throwable.toString()))

                //Populate result with the parsed token body:
                .doOnNext(r -> {
                    final JWT jwt = new JWT(r.accessToken);
                    ((TextView)findViewById(R.id.appAuthLogin_keycloakResponse)).setText(jwt.body);
                })

                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))

                .subscribe();
    }
}