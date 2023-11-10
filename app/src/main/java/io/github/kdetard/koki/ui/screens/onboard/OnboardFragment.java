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
                .paddingTop(WindowInsetsCompat.Type.statusBars(), true)
                .applyToView(view);

        setInsets(view.findViewById(R.id.onboardFragment_actionContainer));

        return view;
    }

    private void setInsets(View parent) {
        final int origPaddingTop = parent.getPaddingTop();
        final int origPaddingBottom = parent.getPaddingBottom();

        Insetter.builder().setOnApplyInsetsListener((view, insets, viewState) -> {
            final Insets navigationBarsInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            final Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            int paddingBottom = imeInsets.bottom;
            if (imeInsets.bottom == 0) {
                paddingBottom = navigationBarsInsets.bottom;
            }

            view.setPaddingRelative(
                    view.getPaddingLeft(),
                    origPaddingTop,
                    view.getPaddingRight(),
                    origPaddingBottom + paddingBottom
            );
        }).applyToView(parent);
    }
}
