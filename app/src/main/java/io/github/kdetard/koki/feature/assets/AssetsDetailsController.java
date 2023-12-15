package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.core.content.ContextCompat;

import com.jakewharton.rxbinding4.view.RxView;
import com.squareup.moshi.JsonAdapter;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerAssetsDetailBinding;
import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;
import timber.log.Timber;

public class AssetsDetailsController extends BottomSheetController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface AssetsDetailsEntryPoint {
        JsonAdapter<Object> objectAdapter();
    }

    ControllerAssetsDetailBinding binding;
    Asset<AssetAttribute> asset;
    AssetsDetailsEntryPoint entryPoint;

    public AssetsDetailsController(Asset<AssetAttribute> asset) {

        super(R.layout.controller_assets_detail);
        this.asset = asset;
    }

    public AssetsDetailsController() {
        super(R.layout.controller_assets_detail);
        this.asset = null;
    }

    @Override
    public void onViewCreated(View view) {
        binding = ControllerAssetsDetailBinding.bind(view);

        super.onViewCreated(view, binding.bottomSheet);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), AssetsDetailsEntryPoint.class);

        if (asset == null)
            return;

        binding.assetsDetailIcon.setImageResource(asset.attributes().toIconResource());
        binding.assetsDetailIcon.setImageTintList(ContextCompat.getColorStateList(view.getContext(), asset.attributes().toTintResource()));
        binding.assetsDetailTitle.setText(asset.name());
        binding.assetsDetailDescription.setText(String.format("Created: %s", new Date(asset.createdOn())));

        binding.assetsDetailRecycler.setNestedScrollingEnabled(false);

        var mapObj = (Map<String, Object>) entryPoint.objectAdapter().nullSafe().toJsonValue(asset);
        if (mapObj != null) {
            for (var entry : mapObj.entrySet()) {
                Timber.d("Entry: %s", entry);
            }
        }

        RxView.layoutChanges(binding.assetsDetailRecycler)
                .doOnNext(u -> getBehavior().setMaxHeight(binding.assetsDetailRecycler.getMeasuredHeight()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
    }
}
