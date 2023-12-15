package io.github.kdetard.koki.feature.auth;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import android.view.autofill.AutofillManager;

import androidx.datastore.rxjava3.RxDataStore;

import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxTextView;
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
import io.github.kdetard.koki.databinding.ControllerSignUpBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.di.NetworkModule;
import io.github.kdetard.koki.keycloak.RxRestKeycloak;
import io.github.kdetard.koki.keycloak.models.JWT;
import io.github.kdetard.koki.keycloak.models.KeycloakConfig;
import io.github.kdetard.koki.keycloak.KeycloakApiService;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.github.kdetard.koki.form.FormUtils;
import io.github.kdetard.koki.form.SignUpFormResult;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;

public class SignUpController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface SignUpEntryPoint {
        JsonAdapter<KeycloakToken> keycloakTokenJsonAdapter();
        KeycloakApiService apiService();
        RxDataStore<Settings> settings();
    }

    public SignUpController() { super(R.layout.controller_sign_up); }

    private SignUpEntryPoint entryPoint;

    private KeycloakConfig mKeycloakConfig;

    private String mUsername;

    private String mEmail;

    private String mPassword;

    private String mConfirmPassword;

    ControllerSignUpBinding binding;

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), SignUpEntryPoint.class);

        binding = ControllerSignUpBinding.bind(view);

        mKeycloakConfig = KeycloakConfig.getDefaultConfig(getApplicationContext());

        final var username = FormUtils.textChanges(binding.signUpControllerUsernameLayout, FormUtils::isValidUsername);
        final var email = FormUtils.textChanges(binding.signUpControllerEmailLayout, FormUtils::isValidEmail);

        final var passwordLayout = binding.signUpControllerPasswordLayout;
        final var password = FormUtils.textChanges(passwordLayout, FormUtils::isValidPassword);

        final var confirmPasswordLayout = binding.signUpControllerConfirmPasswordLayout;

        username
                .doOnNext(v -> v.getInputLayout().setError(v.getError()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        email
                .doOnNext(v -> v.getInputLayout().setError(v.getError()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        password
                .doOnNext(v -> {
                    String error = v.getError();
                    if (!Objects.requireNonNull(confirmPasswordLayout.getEditText()).getText().toString().equals(v.getText())) {
                        error = (error.isEmpty() ? "" : (error + ". ")) + "Password mismatch";
                    }
                    v.getInputLayout().setError(error);
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxTextView
                .textChanges(Objects.requireNonNull(confirmPasswordLayout.getEditText()))
                .skip(1)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(v -> {
                    // workarounds for password revalidation
                    final var passwordEditText = passwordLayout.getEditText();
                    Objects.requireNonNull(passwordEditText).setText(passwordEditText.getText());
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        Observable
                .combineLatest(
                        username,
                        email,
                        password,
                        SignUpFormResult<TextInputLayout>::new
                )
                .doOnNext(result -> {
                    final var confirmPasswordTxt = confirmPasswordLayout.getEditText().getText().toString();
                    final boolean validSignUp = result.username().isSuccess()
                            && result.email().isSuccess()
                            && result.password().isSuccess()
                            && confirmPasswordTxt.equals(result.password().getText());

                    if (validSignUp) {
                        mUsername = result.username().getText();
                        mEmail = result.email().getText();
                        mPassword = result.password().getText();
                        mConfirmPassword = confirmPasswordTxt;
                    }

                    binding.signUpControllerSignupBtn.setEnabled(validSignUp);
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView
                .clicks(binding.signUpControllerSignupBtn)

                .throttleFirst(500, TimeUnit.MILLISECONDS)

                // Clear results
                .doOnNext(v -> {
                    binding.signUpControllerSignupBtn.setEnabled(false);
                    binding.signUpControllerSignupBtn.setText("Signing up...");
                    MMKV.mmkvWithID(NetworkModule.COOKIE_STORE_NAME).clearAll();
                })

                // Start sign up
                .flatMapSingle(v ->
                        RxRestKeycloak.createUser(entryPoint.apiService(), mKeycloakConfig, mUsername, mEmail, mPassword, mConfirmPassword)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(throwable -> {
                                    setAllError(binding, "Cannot sign up. Error: " + throwable.getMessage());
                                    binding.signUpControllerSignupBtn.setText("Sign up");
                                })
                                .onErrorResumeNext(throwable -> Single.never()))

                // Handle sign up
                .flatMapSingle(r -> {
                    switch (r) {
                        case SUCCESS -> {
                            binding.signUpControllerSignupBtn.setText("Sign up success!");
                            MMKV.mmkvWithID(NetworkModule.COOKIE_STORE_NAME).clearAll();
                            return RxRestKeycloak.newSession(entryPoint.apiService(), mKeycloakConfig, mUsername, mPassword)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnError(throwable -> {
                                        setAllError(binding, "Cannot sign in with new account. Error: " + throwable.getMessage());
                                        binding.signUpControllerSignupBtn.setText("Sign up");
                                    })
                                    .onErrorResumeNext(throwable -> Single.never());
                        }
                        case USERNAME_EXISTS -> binding.signUpControllerUsernameLayout.setError(r.toString());
                        case EMAIL_EXISTS -> binding.signUpControllerEmailLayout.setError(r.toString());
                        default -> setAllError(binding, r.toString());
                    }

                    binding.signUpControllerSignupBtn.setText("Sign up");

                    return Single.never();
                })

                // Handle login with new session
                .doOnNext(r -> {
                    final var accessTokenJwt = new JWT(r.accessToken);
                    Timber.d("Access token JWT: %s", accessTokenJwt);

                    binding.signUpControllerSignupBtn.setText("Logged in...");

                    final var keycloakToken = entryPoint.keycloakTokenJsonAdapter().toJson(r);
                    entryPoint.settings().updateDataAsync(s ->
                            Single.just(s.toBuilder().setKeycloakTokenJson(keycloakToken).build()));

                    view.getContext().getSystemService(AutofillManager.class).commit();
                })

                .to(autoDisposable(getScopeProvider()))

                .subscribe();
    }

    private static void setAllError(ControllerSignUpBinding binding, String error) {
        binding.signUpControllerUsernameLayout.setError(" ");
        binding.signUpControllerEmailLayout.setError(" ");
        binding.signUpControllerPasswordLayout.setError(" ");
        binding.signUpControllerConfirmPasswordLayout.setError(error);
    }
}
