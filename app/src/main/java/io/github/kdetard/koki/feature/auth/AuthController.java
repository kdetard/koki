package io.github.kdetard.koki.feature.auth;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import android.widget.Toast;

import com.bluelinelabs.conductor.RouterTransaction;
import com.jakewharton.rxbinding4.view.RxView;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.databinding.ControllerOnboardAuthBinding;

public class AuthController extends BaseController {
    ControllerOnboardAuthBinding binding;

    public AuthController() {
        super(R.layout.controller_onboard_auth);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        binding = ControllerOnboardAuthBinding.bind(view);

        RxView
                .clicks(binding.authControllerSignInWithGoogleBtn)
                .doOnNext(v -> Toast.makeText(getApplicationContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView
                .clicks(binding.authControllerSignUpBtn)
                .doOnNext(v -> getRouter().pushController(
                        RouterTransaction.with(new SignUpController())
                ))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView
                .clicks(binding.authControllerSignInBtn)
                .doOnNext(v -> getRouter().pushController(
                        RouterTransaction.with(new SignInController())
                ))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
    }
}
