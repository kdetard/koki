package io.github.kdetard.koki.ui.screens.SignIn;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.jakewharton.rxbinding4.view.RxView;
import com.tencent.mmkv.MMKV;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.di.NetworkModule;
import io.github.kdetard.koki.di.RxRestKeycloak;
import io.github.kdetard.koki.model.JWT;
import io.github.kdetard.koki.model.Keycloak.KeycloakConfig;
import io.github.kdetard.koki.network.KeycloakApiService;
import io.github.kdetard.koki.ui.screens.auth.AuthFragmentDirections;
import io.github.kdetard.koki.ui.screens.onboard.OnboardFragment;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

@AndroidEntryPoint
public class SignInFragment extends OnboardFragment {
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
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mKeycloakConfig = KeycloakConfig.getDefaultConfig(view.getContext());

        final NavController navController = Navigation.findNavController(view);

        RxView
                .clicks(view.findViewById(R.id.restLogin_registerBtn))
                .doOnNext(v -> navController.navigate(AuthFragmentDirections.actionGlobalSignUpFragment()))
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe();

        RxView
                .clicks(view.findViewById(R.id.restLogin_resetPasswordBtn))
                .doOnNext(v -> Toast.makeText(requireContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe();

        RxView
                //Capture Login Button Click:
                .clicks(view.findViewById(R.id.restLogin_loginBtn))

                .throttleFirst(500, TimeUnit.MILLISECONDS)

                //Clear Results
                .doOnNext(v -> {
                    mPassword = ((TextInputEditText)view.findViewById(R.id.restLogin_passwordTxt)).getText().toString();
                    mUsername = ((TextInputEditText)view.findViewById(R.id.restLogin_usernameTxt)).getText().toString();
                    ((TextView)view.findViewById(R.id.restLogin_keycloakResponse)).setText("");
                    MMKV.mmkvWithID(NetworkModule.COOKIE_STORE_NAME).clearAll();
                })

                .subscribeOn(AndroidSchedulers.mainThread())

                //Start Auth:
                .flatMapSingle(v ->
                        RxRestKeycloak.newSession(apiService, mKeycloakConfig, mUsername, mPassword)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(throwable ->
                                        ((TextView) view.findViewById(R.id.restLogin_keycloakResponse)).setText(throwable.toString()))
                                .onErrorResumeNext(throwable -> Single.never()))

                //Populate result with the parsed token body:
                .doOnNext(r -> {
                    final JWT jwt = new JWT(r.accessToken);
                    ((TextView)view.findViewById(R.id.restLogin_keycloakResponse)).setText(jwt.body);
                })

                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))

                .subscribe();
    }
}