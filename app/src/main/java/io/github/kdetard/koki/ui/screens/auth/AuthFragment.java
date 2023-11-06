package io.github.kdetard.koki.ui.screens.auth;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

@AndroidEntryPoint
public class AuthFragment extends Fragment {
    public AuthFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavController navController = Navigation.findNavController(view);

        RxView
                .clicks(view.findViewById(R.id.authFragment_signInWithGoogleBtn))
                .doOnNext(v -> Toast.makeText(requireContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();

        RxView
                .clicks(view.findViewById(R.id.authFragment_signUpBtn))
                .doOnNext(v -> navController.navigate(R.id.action_authFragment_to_signUpFragment))
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