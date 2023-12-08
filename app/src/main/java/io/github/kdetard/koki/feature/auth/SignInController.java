package io.github.kdetard.koki.feature.auth;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import android.widget.Toast;

import androidx.datastore.rxjava3.RxDataStore;

import com.bluelinelabs.conductor.RouterTransaction;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.view.RxView;
import com.squareup.moshi.JsonAdapter;
import com.tencent.mmkv.MMKV;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.databinding.ControllerSignInBinding;
import io.github.kdetard.koki.databinding.ControllerSignUpBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.di.NetworkModule;
import io.github.kdetard.koki.keycloak.RxRestKeycloak;
import io.github.kdetard.koki.keycloak.models.JWT;
import io.github.kdetard.koki.keycloak.models.KeycloakConfig;
import io.github.kdetard.koki.keycloak.KeycloakApiService;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.github.kdetard.koki.utils.FormUtils;
import io.github.kdetard.koki.utils.SignInFormResult;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;

public class SignInController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface SignInEntryPoint {
        JsonAdapter<KeycloakToken> keycloakTokenJsonAdapter();
        KeycloakApiService apiService();
        RxDataStore<Settings> settings();
    }

    SignInEntryPoint entryPoint;

    KeycloakConfig mKeycloakConfig;

    String mUsername;

    String mPassword;

    ControllerSignInBinding binding;

    public SignInController() {
        super(R.layout.controller_sign_in);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), SignInEntryPoint.class);

        binding = ControllerSignInBinding.bind(view);

        mKeycloakConfig = KeycloakConfig.getDefaultConfig(getApplicationContext());

        RxView
                .clicks(binding.signInControllerRegisterBtn)
                .doOnNext(v -> getRouter().pushController(RouterTransaction.with(new SignUpController())))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView
                .clicks(binding.signInControllerResetPasswordBtn)
                .doOnNext(v -> Toast.makeText(getApplicationContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        final var username = FormUtils.textChanges(binding.signInControllerUsernameLayout, FormUtils::isValidUsernameOrEmail);
        final var password = FormUtils.textChanges(binding.signInControllerPasswordLayout, FormUtils::isValidPassword);

        username
                .doOnNext(v -> v.getInputLayout().setError(v.getError()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        password
                .doOnNext(v -> v.getInputLayout().setError(v.getError()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        Observable
                .combineLatest(
                        username,
                        password,
                        SignInFormResult<TextInputLayout>::new
                )
                .doOnNext(result -> {
                    final boolean validSignIn = result.getUserName().isSuccess() && (result.getPassword().isSuccess() || result.getPassword().isStrict());
                    binding.signInControllerLoginBtn.setEnabled(validSignIn);
                    if (validSignIn) {
                        mUsername = result.getUserName().getText();
                        mPassword = result.getPassword().getText();
                    }
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView
                .clicks(binding.signInControllerLoginBtn)

                .throttleFirst(500, TimeUnit.MILLISECONDS)

                // Clear results
                .doOnNext(v -> {
                    binding.signInControllerLoginBtn.setEnabled(false);
                    binding.signInControllerLoginBtn.setText("Logging in...");
                    binding.signInControllerKeycloakResponse.setText("");
                    MMKV.mmkvWithID(NetworkModule.COOKIE_STORE_NAME).clearAll();
                })

                // Start auth
                .flatMapSingle(v ->
                        RxRestKeycloak.newSession(entryPoint.apiService(), mKeycloakConfig, mUsername, mPassword)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(throwable -> {
                                    setAllError(binding, "Cannot sign in. Error: " + throwable.getMessage());
                                    binding.signInControllerLoginBtn.setText("Log in");
                                })
                                .onErrorResumeNext(throwable -> Single.never()))

                // Populate result with the parsed token body:
                .doOnNext(r -> {
                    final var accessTokenJwt = new JWT(r.accessToken);
                    Timber.d("Access token JWT: %s", accessTokenJwt);

                    binding.signInControllerLoginBtn.setText("Logged in...");

                    final var keycloakToken = entryPoint.keycloakTokenJsonAdapter().toJson(r);
                    entryPoint.settings().updateDataAsync(s ->
                            Single.just(s.toBuilder().setKeycloakTokenJson(keycloakToken).build()));
                })

                .to(autoDisposable(getScopeProvider()))

                .subscribe();
    }

    private static void setAllError(ControllerSignInBinding binding, String error) {
        binding.signInControllerUsernameLayout.setError(" ");
        binding.signInControllerPasswordLayout.setError(error);
    }
}
