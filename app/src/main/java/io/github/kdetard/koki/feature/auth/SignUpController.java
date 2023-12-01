package io.github.kdetard.koki.feature.auth;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Build;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.datastore.rxjava3.RxDataStore;

import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxTextView;
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
import io.github.kdetard.koki.keycloak.models.KeycloakConfig;
import io.github.kdetard.koki.keycloak.KeycloakApiService;
import io.github.kdetard.koki.utils.FormResult;
import io.github.kdetard.koki.utils.FormUtils;
import io.github.kdetard.koki.utils.SignUpFormResult;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import timber.log.Timber;

public class SignUpController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface SignUpEntryPoint {
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

        Observable<FormResult<TextInputLayout>> username = FormUtils.textChanges(binding.signUpControllerUsernameLayout, FormUtils::isValidUsername);
        Observable<FormResult<TextInputLayout>> email = FormUtils.textChanges(binding.signUpControllerEmailLayout, FormUtils::isValidEmail);

        TextInputLayout passwordLayout = binding.signUpControllerPasswordLayout;
        Observable<FormResult<TextInputLayout>> password = FormUtils.textChanges(passwordLayout, FormUtils::isValidPassword);

        TextInputLayout confirmPasswordLayout = binding.signUpControllerConfirmPasswordLayout;

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
                    // hacky workarounds for password revalidation
                    final EditText passwordEditText = passwordLayout.getEditText();
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
                    final String confirmPasswordTxt = confirmPasswordLayout.getEditText().getText().toString();
                    final boolean validSignUp = result.getUsername().isSuccess()
                            && result.getEmail().isSuccess()
                            && result.getPassword().isSuccess()
                            && confirmPasswordTxt.equals(result.getPassword().getText());

                    if (validSignUp) {
                        mUsername = result.getUsername().getText();
                        mEmail = result.getEmail().getText();
                        mPassword = result.getPassword().getText();
                        mConfirmPassword = confirmPasswordTxt;
                    }

                    binding.signUpControllerSignupBtn.setEnabled(validSignUp);
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView
                //Capture Login Button Click:
                .clicks(binding.signUpControllerSignupBtn)

                .throttleFirst(500, TimeUnit.MILLISECONDS)

                //Clear Results
                .doOnNext(v -> {
                    binding.signUpControllerSignupBtn.setEnabled(false);
                    binding.signUpControllerSignupBtn.setText("Signing up...");
                    binding.signUpControllerKeycloakResponse.setText("");
                    MMKV.mmkvWithID(NetworkModule.COOKIE_STORE_NAME).clearAll();
                })

                //Start Auth:
                .flatMapMaybe(v ->
                        RxRestKeycloak.createUser(entryPoint.apiService(), mKeycloakConfig, mUsername, mEmail, mPassword, mConfirmPassword)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(throwable -> {
                                    Timber.d("Sign up error");
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                    binding.signUpControllerSignupBtn.setEnabled(false);
                                    binding.signUpControllerSignupBtn.setText("Sign up");
                                    binding.signUpControllerKeycloakResponse.setText(throwable.toString());
                                })
                                .onErrorResumeNext(throwable -> Maybe.empty()))

                .doOnNext(r -> {
                    Timber.d("Sign up success");
                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    binding.signUpControllerSignupBtn.setText("Sign up success!");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        view.getContext().getSystemService(AutofillManager.class).commit();
                    }
                })

                .to(autoDisposable(getScopeProvider()))

                .subscribe();
    }
}
