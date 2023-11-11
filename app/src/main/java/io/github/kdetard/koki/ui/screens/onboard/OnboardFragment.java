package io.github.kdetard.koki.ui.screens.onboard;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jakewharton.rxbinding4.view.RxView;

import java.util.Objects;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;

import dev.chrisbanes.insetter.Insetter;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.ui.base.BaseFragment;

@AndroidEntryPoint
public class OnboardFragment extends BaseFragment {
    private static final int OutOfBoundInsetType = 10; /* WindowInsetsCompat.Type.SIZE + 1 */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_onboard, container, false);

        RxView
                .clicks(view.findViewById(R.id.onboardFragment_appBarLayout_langPickerBtn))
                .doOnNext(v -> Toast.makeText(requireContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe();

        Insetter.builder()
                .paddingTop(WindowInsetsCompat.Type.systemBars(), true)
                .applyToView(view.findViewById(R.id.onboardFragment_appBarLayout));

        setInsets(view.findViewById(R.id.onboardFragment_actionLayout));

        return view;
    }

    private Insetter.Builder insetBuilder() {
        return Insetter.builder()
                .paddingBottom(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime(), true);
    }

    private void setInsets(View parent) {
        parent.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final int statusBarInsets = Objects.requireNonNull(ViewCompat.getRootWindowInsets(v))
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top;

            int topInsetType = OutOfBoundInsetType;
            if (top <= statusBarInsets) {
                topInsetType = WindowInsetsCompat.Type.statusBars();
            }

            insetBuilder()
                    .paddingTop(topInsetType, true)
                    .applyToView(v);
        });
    }
}
