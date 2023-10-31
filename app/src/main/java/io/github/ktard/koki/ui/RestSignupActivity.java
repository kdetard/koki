package io.github.ktard.koki.ui;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.ktard.koki.R;
import io.github.ktard.koki.di.RxRestKeycloak;
import io.github.ktard.koki.model.Keycloak.KeycloakConfig;
import io.github.ktard.koki.network.KeycloakApiService;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import timber.log.Timber;

@AndroidEntryPoint
public class RestSignupActivity extends AppCompatActivity {
    // Internal cached instance of the Keycloak config:
    private KeycloakConfig mKeycloakConfig;

    private String mUsername;

    private String mEmail;

    private String mPassword;

    private String mConfirmPassword;

    @Inject
    KeycloakApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply View:
        setContentView(R.layout.activity_rest_signup);

        mKeycloakConfig = KeycloakConfig.getDefaultConfig(this);

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.restSignup_signupBtn))

                //Clear Results
                .doOnNext(v -> {
                    mUsername = ((TextInputEditText)findViewById(R.id.restSignup_usernameTxt)).getText().toString();
                    mEmail = ((TextInputEditText)findViewById(R.id.restSignup_emailTxt)).getText().toString();
                    mPassword = ((TextInputEditText)findViewById(R.id.restSignup_passwordTxt)).getText().toString();
                    mConfirmPassword = ((TextInputEditText)findViewById(R.id.restSignup_confirmPasswordTxt)).getText().toString();
                    ((TextView)findViewById(R.id.restSignup_keycloakResponse)).setText("Signing up...");
                    Timber.d("Clicked");
                })

                //Start Auth:
                .flatMapMaybe(v ->
                        RxRestKeycloak.createUser(apiService, mKeycloakConfig, mUsername, mEmail, mPassword, mConfirmPassword)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(throwable -> {
                                    Timber.d("Sign up error");
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                    ((TextView)findViewById(R.id.restSignup_keycloakResponse)).setText(throwable.toString());
                                })
                                .onErrorResumeNext(throwable -> Maybe.just(false)))

                .doOnNext(r -> {
                    var status = r ? "success" : "error";
                    Timber.d("Sign up %s", status);
                    Toast.makeText(getApplicationContext(), r ? "Success" : "Error", Toast.LENGTH_SHORT).show();
                    ((TextView)findViewById(R.id.restSignup_keycloakResponse)).setText(String.format("Sign up %s!", status));
                })

                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))

                .subscribe();
    }
}