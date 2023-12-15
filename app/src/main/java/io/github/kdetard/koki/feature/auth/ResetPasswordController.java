package io.github.kdetard.koki.feature.auth;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import com.jakewharton.rxbinding4.view.RxView;
import com.tencent.mmkv.MMKV;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerResetPasswordBinding;
import io.github.kdetard.koki.di.NetworkModule;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.form.FormUtils;
import io.github.kdetard.koki.form.ResetPasswordFormResult;
import io.github.kdetard.koki.keycloak.KeycloakApiService;
import io.github.kdetard.koki.keycloak.RxRestKeycloak;
import io.github.kdetard.koki.keycloak.models.KeycloakConfig;
import io.github.kdetard.koki.keycloak.models.ResetPasswordResult;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;

public class ResetPasswordController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface ResetPasswordEntryPoint {
        KeycloakApiService apiService();
    }

    ResetPasswordEntryPoint entryPoint;

    ControllerResetPasswordBinding binding;

    KeycloakConfig mKeycloakConfig;

    String mUsernameOrEmail;

    public ResetPasswordController() {
        super(R.layout.controller_reset_password);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), ResetPasswordEntryPoint.class);

        binding = ControllerResetPasswordBinding.bind(view);

        mKeycloakConfig = KeycloakConfig.getDefaultConfig(getApplicationContext());

        final var username = FormUtils.textChanges(binding.resetPasswordControllerUsernameLayout, FormUtils::isValidUsernameOrEmail);

        username
                .map(ResetPasswordFormResult::new)
                .doOnNext(result -> {
                    binding.resetPasswordControllerResetPasswordBtn.setText(R.string.reset_password_action);
                    result.userNameOrEmail().getInputLayout().setError(result.userNameOrEmail().getError());
                    binding.resetPasswordControllerResetPasswordBtn.setEnabled(result.userNameOrEmail().isSuccess());
                    if (result.userNameOrEmail().isSuccess()) {
                        mUsernameOrEmail = result.userNameOrEmail().getText();
                    }
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView
                .clicks(binding.resetPasswordControllerResetPasswordBtn)

                .throttleFirst(500, TimeUnit.MILLISECONDS)

                // Clear results
                .doOnNext(v -> {
                    binding.resetPasswordControllerResetPasswordBtn.setEnabled(false);
                    binding.resetPasswordControllerResetPasswordBtn.setText("Hang on tight...");
                    MMKV.mmkvWithID(NetworkModule.COOKIE_STORE_NAME).clearAll();
                })

                // Start auth
                .flatMapSingle(v ->
                        RxRestKeycloak.resetPassword(entryPoint.apiService(), mKeycloakConfig, mUsernameOrEmail)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError(throwable -> {
                                    binding.resetPasswordControllerUsernameLayout.setError("Cannot reset password. Error: " + throwable.getMessage());
                                    binding.resetPasswordControllerResetPasswordBtn.setText("Reset password");
                                })
                                .onErrorResumeNext(throwable -> Single.never()))

                // Populate result with the parsed token body:
                .doOnNext(r -> {
                    var msg = switch (r) {
                        case SUCCESS -> "Password reset link sent to your email";
                        case NULL, EMPTY -> "Password reset link was empty";
                        case UNKNOWN -> "Unknown error while getting password reset link";
                        case FAILED_TO_SEND_MAIL -> "Failed to send password reset link";
                        case SPECIFY -> "Please specify your username or email";
                        case TIMEOUT -> "Request timed out";
                    };
                    if (r == ResetPasswordResult.SUCCESS) {
                        binding.resetPasswordControllerUsernameLayout.setError(null);
                        binding.resetPasswordControllerResetPasswordBtn.setText(msg);
                    } else {
                        binding.resetPasswordControllerUsernameLayout.setError(msg);
                        binding.resetPasswordControllerResetPasswordBtn.setText(R.string.reset_password_action);
                    }
                })

                .to(autoDisposable(getScopeProvider()))

                .subscribe();
    }
}
