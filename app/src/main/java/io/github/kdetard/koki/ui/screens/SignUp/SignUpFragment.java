package io.github.kdetard.koki.ui.screens.SignUp;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;

import java.util.Objects;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.di.RxRestKeycloak;
import io.github.kdetard.koki.model.Keycloak.KeycloakConfig;
import io.github.kdetard.koki.network.KeycloakApiService;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import timber.log.Timber;

@AndroidEntryPoint
public class SignUpFragment extends Fragment {
    // Internal cached instance of the Keycloak config:
    private KeycloakConfig mKeycloakConfig;

    private String mUsername;

    private String mEmail;

    private String mPassword;

    private String mConfirmPassword;

    @Inject
    KeycloakApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mKeycloakConfig = KeycloakConfig.getDefaultConfig(view.getContext());

        RxView
                //Capture Login Button Click:
                .clicks(view.findViewById(R.id.restSignup_signupBtn))

                //Clear Results
                .doOnNext(v -> {
                    mUsername = ((TextInputEditText)view.findViewById(R.id.restSignup_usernameTxt)).getText().toString();
                    mEmail = ((TextInputEditText)view.findViewById(R.id.restSignup_emailTxt)).getText().toString();
                    mPassword = ((TextInputEditText)view.findViewById(R.id.restSignup_passwordTxt)).getText().toString();
                    mConfirmPassword = ((TextInputEditText)view.findViewById(R.id.restSignup_confirmPasswordTxt)).getText().toString();
                    ((TextView)view.findViewById(R.id.restSignup_keycloakResponse)).setText("Signing up...");
                    Timber.d("Clicked");
                })

                //Start Auth:
                .flatMapMaybe(v ->
                        RxRestKeycloak.createUser(apiService, mKeycloakConfig, mUsername, mEmail, mPassword, mConfirmPassword)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(throwable -> {
                                    Timber.d("Sign up error");
                                    Toast.makeText( requireContext().getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                    ((TextView)view.findViewById(R.id.restSignup_keycloakResponse)).setText(throwable.toString());
                                })
                                .onErrorResumeNext(throwable -> Maybe.empty()))

                .doOnNext(r -> {
                    final String status = r ? "success" : "error";
                    Timber.d("Sign up %s", status);
                    Toast.makeText( requireContext().getApplicationContext(), r ? "Success" : "Error", Toast.LENGTH_SHORT).show();
                    ((TextView)view.findViewById(R.id.restSignup_keycloakResponse)).setText(String.format("Sign up %s!", status));
                })

                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))

                .subscribe();
    }
}