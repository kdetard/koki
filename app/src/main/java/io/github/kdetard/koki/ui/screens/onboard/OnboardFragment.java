package io.github.kdetard.koki.ui.screens.onboard;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.LayoutInflaterCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.rxbinding4.view.RxView;
import com.tencent.mmkv.MMKV;

import java.util.Objects;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;

@AndroidEntryPoint
public class OnboardFragment extends Fragment {
    public OnboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_onboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RxView
                .clicks(view.findViewById(R.id.onboardFragment_introLayout_langPickerBtn))
                .doOnNext(v -> Toast.makeText(requireContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();
    }
}
