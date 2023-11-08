package io.github.kdetard.koki.ui.screens.onboard;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.rxbinding4.view.RxView;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import dagger.hilt.android.AndroidEntryPoint;

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
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();

        return view;
    }

    protected View requireParent() {
        return requireParentFragment().requireParentFragment().requireView();
    }
}
