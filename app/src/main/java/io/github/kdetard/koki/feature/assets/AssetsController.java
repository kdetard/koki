package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import autodispose2.MaybeSubscribeProxy;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerAssetsBinding;
import io.github.kdetard.koki.feature.map.MapController;
import io.github.kdetard.koki.feature.map.OnMapListener;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;
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

    List<Asset<AssetAttribute>> assets;
    List<Asset<AssetAttribute>> locationAssets;
    MaybeSubscribeProxy<List<Asset<AssetAttribute>>> assetsSubscription;
    MapView mapView;

    public AssetsController() { super(R.layout.controller_assets); }

    @Override
    public void onViewCreated(View view) {
        binding = ControllerAssetsBinding.bind(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), AssetsEntryPoint.class);

        if (childRouter == null)
            childRouter = getChildRouter(binding.assetsOverviewContainer)
                    .setPopRootControllerMode(Router.PopRootControllerMode.POP_ROOT_CONTROLLER_BUT_NOT_VIEW);

        if (!childRouter.hasRootController()) {
            childRouter.setRoot(RouterTransaction.with(new AssetsOverviewController())
                    .pushChangeHandler(new FadeChangeHandler())
                    .popChangeHandler(new FadeChangeHandler()));
        }

        if (assets != null && !assets.isEmpty()) {
            onAssetsAvailable(assets);
            return;
        }

        invalidate();
    }

    private void invalidate() {
        if (assetsSubscription == null)
            assetsSubscription = entryPoint.service().getAssets()
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete(throwable -> {
                    binding.assetsProgressIndicator.hide();
                    getRouter().pushController(RouterTransaction.with(new AssetsUnavailableController()));
                    return true;
                })
                .doOnSuccess(this::onAssetsAvailable)
                .to(autoDisposable(getScopeProvider()));

        assetsSubscription.subscribe();
    }

    private void onAssetsAvailable(@NonNull List<Asset<AssetAttribute>> assets) {
        this.assets = assets;

        if (locationAssets == null || locationAssets.isEmpty())
            locationAssets = assets.stream()
                    .filter(asset -> asset.attributes().location() != null && asset.attributes().location().value() != null)
                    .collect(Collectors.toList());

        childRouter.getBackstack()
                .stream()
                .reduce((a, b) -> b)
                .filter(c -> c.controller() instanceof OnAssetsAvailableListener)
                .ifPresent(c -> ((OnAssetsAvailableListener) c.controller()).setAssets(assets));

        assert getView() != null;
        mapView = new MapView(getView().getContext());
        binding.getRoot().addView(mapView, 0);
        binding.assetsProgressIndicator.hide();
        super.onViewCreated(getView());
    }

    @Override
    public void onMapReady() {
        var symbols = locationAssets.stream().map(a -> a.attributes().toSymbol());
        getSymbolManager().create(symbols.collect(Collectors.toList()));

        childRouter.getBackstack()
                .stream()
                .reduce((a, b) -> b)
                .filter(c -> c.controller() instanceof OnMapListener)
                .ifPresent(c -> ((OnMapListener) c.controller()).onMapReady(getMapboxMap()));
    }

    @Override
    public boolean onSymbolClick(@NonNull Symbol symbol) {
        if (locationAssets != null && !locationAssets.isEmpty()) {
            if (childRouter.getBackstack().stream().reduce((a, b) -> b).filter(c -> c.controller() instanceof AssetsDetailsController).isPresent())
                childRouter.popCurrentController();
            childRouter.pushController(RouterTransaction.with(
                    new AssetsDetailsController(locationAssets.stream().skip(symbol.getId()).findFirst().orElse(null))));
        }

        childRouter.getBackstack()
                .stream()
                .reduce((a, b) -> b)
                .filter(c -> c.controller() instanceof OnMapListener)
                .ifPresent(c -> ((OnMapListener) c.controller()).onSymbolClick(getMapboxMap()));

        return false;
    }

    @Override
    public MapView getMapView() { return mapView; }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }
}
