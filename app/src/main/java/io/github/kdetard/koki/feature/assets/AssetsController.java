package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import androidx.appcompat.widget.Toolbar;

import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.Objects;
import java.util.stream.Collectors;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerAssetsBinding;
import io.github.kdetard.koki.feature.map.MapController;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class AssetsController extends MapController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface AssetsEntryPoint {
        OpenRemoteService service();
    }

    AssetsEntryPoint entryPoint;

    ControllerAssetsBinding binding;

    Router childRouter;

    public AssetsController() { super(R.layout.controller_assets); }

    @Override
    public void onViewCreated(View view) {
        binding = ControllerAssetsBinding.bind(view);

        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), AssetsEntryPoint.class);

        childRouter = getChildRouter(binding.assetsOverviewContainer)
                .setPopRootControllerMode(Router.PopRootControllerMode.POP_ROOT_CONTROLLER_BUT_NOT_VIEW);

        if (!childRouter.hasRootController()) {
            childRouter.setRoot(RouterTransaction.with(new AssetsOverviewController())
                    .pushChangeHandler(new FadeChangeHandler())
                    .popChangeHandler(new FadeChangeHandler()));
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap, Style style) {
        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(10.8702012, 106.8030358))
                .zoom(15.0)
                .build());

        entryPoint.service().getAssets()
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete(throwable -> {
                    childRouter.pushController(RouterTransaction.with(new AssetsUnavailableController()));
                    return true;
                })
                .doOnSuccess(assets -> {
                    var locationAssets = assets.stream()
                            .filter(asset -> asset.attributes().location() != null && asset.attributes().location().value() != null)
                            .collect(Collectors.toList());

                    var symbols = locationAssets.stream().map(a -> a.attributes().toSymbol());

                    getSymbolManager().create(symbols.collect(Collectors.toList()));

                    getSymbolManager().addClickListener(symbol -> {
                        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                                .target(symbol.getLatLng())
                                .zoom(17.0)
                                .tilt(mapboxMap.getCameraPosition().tilt)
                                .bearing(mapboxMap.getCameraPosition().bearing)
                                .build());

                        childRouter.pushController(RouterTransaction.with(
                                new AssetsDetailsController(locationAssets.stream().skip(symbol.getId()).findFirst().orElse(null))));

                        return false;
                    });

                    childRouter.getBackstack()
                            .stream()
                            .reduce((a, b) -> b)
                            .filter(c -> c.controller() instanceof OnAssetsAvailableListener)
                            .ifPresent(currentListener -> ((OnAssetsAvailableListener) currentListener.controller()).setAssets(assets));
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
    }

    @Override
    public MapView getMapView() { return binding.mapView; }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }

    /*@Override
    public void onConfigurationChange(@NonNull Configuration newConfig) {
        super.onConfigurationChange(newConfig);
        bottomSheetBehavior.setPeekHeight(binding.assetsSheetControllerContainer.getMeasuredHeight());
        bottomSheetBehavior.setMaxHeight(binding.assetsSheetControllerContainer.getMeasuredHeight());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
    }*/
}
