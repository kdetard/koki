package io.github.kdetard.koki.ui.screens.onboard;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.graphics.Insets;
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

    private void setInsets(View parent) {
        Insetter.builder().setOnApplyInsetsListener((view, insets, viewState) -> {
            final Insets statusBarsInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            final Insets navigationBarsInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            final Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            int mPaddingTop;
            int mPaddingBottom;

            if (imeInsets.bottom == 0) {
                mPaddingTop = 0;
                mPaddingBottom = navigationBarsInsets.bottom;
                view.setBackgroundColor(0);
                view.setBackgroundResource(R.drawable.action_container_background);
            } else {
                mPaddingTop = statusBarsInsets.top;
                mPaddingBottom = imeInsets.bottom;
                view.setBackgroundColor(Objects.requireNonNull(view.getBackgroundTintList()).getDefaultColor());
            }

            view.setPaddingRelative(
                    view.getPaddingLeft(),
                    mPaddingTop,
                    view.getPaddingRight(),
                    mPaddingBottom
            );
        }).applyToView(parent);
    }
}
