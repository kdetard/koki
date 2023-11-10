package io.github.kdetard.koki.ui.screens.SignUp;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;
import com.tencent.mmkv.MMKV;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.di.NetworkModule;
import io.github.kdetard.koki.di.RxRestKeycloak;
import io.github.kdetard.koki.model.Keycloak.KeycloakConfig;
import io.github.kdetard.koki.network.KeycloakApiService;
import io.github.kdetard.koki.ui.screens.onboard.OnboardFragment;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import timber.log.Timber;

@AndroidEntryPoint
public class SignUpFragment extends OnboardFragment {
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
                    MMKV.mmkvWithID(NetworkModule.COOKIE_STORE_NAME).clearAll();
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
                    Timber.d("Sign up success");
                    Toast.makeText( requireContext().getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    ((TextView)view.findViewById(R.id.restSignup_keycloakResponse)).setText("Sign up success!");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        requireContext().getSystemService(AutofillManager.class).commit();
                    }
                })

                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))

                .subscribe();
    }
}