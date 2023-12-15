package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import com.jakewharton.rxbinding4.view.RxView;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerAssetsUnavailableBinding;

public class AssetsUnavailableController extends BottomSheetController {
    ControllerAssetsUnavailableBinding binding;
    public AssetsUnavailableController() { super(R.layout.controller_assets_unavailable); }

    @Override
    public void onViewCreated(View view) {
        binding = ControllerAssetsUnavailableBinding.bind(view);
        super.onViewCreated(view);
        RxView.layoutChanges(binding.assetsUnavailableContainer)
                .skip(1)
                .doOnNext(u -> behavior.setMaxHeight(binding.assetsUnavailableContainer.getMeasuredHeight()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
    }
}
