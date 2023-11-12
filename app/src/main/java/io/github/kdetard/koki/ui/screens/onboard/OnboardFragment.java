package io.github.kdetard.koki.ui.screens.onboard;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private static final int LandscapeInsetType = WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.navigationBars();
    private static final int PortraitInsetType = WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime();
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

        setPortraitInsets(view.findViewById(R.id.onboardFragment_actionLayout));

        return view;
    }

    private static void setPortraitInsets(View actionLayout) {
        final Drawable background = actionLayout.getBackground();

        actionLayout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final int statusBarInsets = Objects.requireNonNull(ViewCompat.getRootWindowInsets(v))
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top;

            final boolean isBehindStatusBar = top <= statusBarInsets;
            final int topInsetType = isBehindStatusBar ? WindowInsetsCompat.Type.statusBars() : OutOfBoundInsetType;

            toggleBackgroundTint(v, background, isBehindStatusBar);

            Insetter.builder()
                    .paddingTop(topInsetType, true)
                    .paddingBottom(PortraitInsetType, true)
                    .applyToView(v);
        });
    }

    private static void toggleBackgroundTint(View view, Drawable originalBackground, boolean pred) {
        if (pred) {
            view.setBackgroundColor(Objects.requireNonNull(view.getBackgroundTintList()).getDefaultColor());
        } else {
            view.setBackgroundColor(0);
            view.setBackground(originalBackground);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        final int orientationInsetType = isLandscape ? LandscapeInsetType : OutOfBoundInsetType;

        Insetter.builder()
                .paddingLeft(orientationInsetType, true)
                .paddingRight(orientationInsetType, true)
                .applyToView(requireActivity().findViewById(R.id.rootFragment_actionLayout));
    }
}
