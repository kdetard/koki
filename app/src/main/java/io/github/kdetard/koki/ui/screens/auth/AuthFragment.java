package io.github.kdetard.koki.ui.screens.auth;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.rxbinding4.view.RxView;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.ui.screens.onboard.OnboardFragment;

@AndroidEntryPoint
public class AuthFragment extends OnboardFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        final View view = requireView();

        final NavController navController = Navigation.findNavController(view);

        RxView
                .clicks(view.findViewById(R.id.authFragment_signInWithGoogleBtn))
                .doOnNext(v -> Toast.makeText(requireContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();

        RxView
                .clicks(view.findViewById(R.id.authFragment_signUpBtn))
                .doOnNext(v -> navController.navigate(R.id.action_global_signUpFragment))
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();

        RxView
                .clicks(view.findViewById(R.id.authFragment_signInBtn))
                .doOnNext(v -> navController.navigate(R.id.action_authFragment_to_signInFragment))
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();

        RxView
                .clicks(view.findViewById(R.id.authFragment_resetPasswordBtn))
                .doOnNext(v -> Toast.makeText(requireContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();
    }
}