package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.jakewharton.rxbinding4.view.RxView;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapboxMap;
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
import io.github.kdetard.koki.feature.map.OnMapListener;
import io.github.kdetard.koki.map.MapUtils;
import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class AssetsDetailsController extends BottomSheetController implements OnMapListener {
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
        binding.assetsDetailId.setText(asset.id());
        binding.assetsDetailTitle.setText(asset.name());
        binding.assetsDetailDescription.setText(String.format("Created: %s", new Date(asset.createdOn())));

        binding.assetsDetailRecycler.setNestedScrollingEnabled(false);

        var mapObj = (Map<String, Object>) entryPoint.objectAdapter().nullSafe().toJsonValue(asset);
        if (mapObj != null) {
            for (var entry : mapObj.entrySet()) {
                Timber.d("Entry: %s", entry);
            }
        }

        RxView.clicks(binding.assetsDetailId)
                .debounce(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(u -> {
                    var clipboard = ContextCompat.getSystemService(view.getContext(), ClipboardManager.class);
                    var toast = "Failed to copy asset id to clipboard";
                    if (clipboard != null) {
                        toast = "Copied asset id to clipboard";
                        clipboard.setPrimaryClip(ClipData.newPlainText("Asset id", asset.id()));
                    }
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || clipboard == null) {
                        Toast.makeText(view.getContext(), toast, Toast.LENGTH_SHORT).show();
                    }
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView.layoutChanges(binding.assetsDetailRecycler)
                .doOnNext(u -> getBehavior().setMaxHeight(binding.assetsDetailRecycler.getMeasuredHeight()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        Timber.d("onMapReady");

        var cameraBuilder = new CameraPosition.Builder().target(MapUtils.DEFAULT_CENTER)
                .zoom(15.0)
                .tilt(mapboxMap.getCameraPosition().tilt)
                .bearing(mapboxMap.getCameraPosition().bearing);

        if (asset.attributes().location() != null && asset.attributes().location().value() != null) {
            cameraBuilder.target(asset.attributes().location().value().toLatLng())
                    .zoom(17.0);
        }

        mapboxMap.animateCamera(_mapboxMap -> cameraBuilder.build());
    }

    @Override
    public void onSymbolClick(MapboxMap mapboxMap) { onMapReady(mapboxMap); }
}
