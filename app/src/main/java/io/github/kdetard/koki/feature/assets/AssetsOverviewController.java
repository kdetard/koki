package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding4.view.RxView;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import autodispose2.ObservableSubscribeProxy;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerAssetsOverviewBinding;
import io.github.kdetard.koki.feature.base.GridSpacingItemDecoration;
import io.github.kdetard.koki.feature.map.OnMapListener;
import io.github.kdetard.koki.map.MapUtils;
import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class AssetsOverviewController extends BottomSheetController implements OnAssetsAvailableListener, OnMapListener {
    BehaviorSubject<List<Asset<AssetAttribute>>> assets;
    ObservableSubscribeProxy<List<AssetItem>> assetsSubscription;

    ControllerAssetsOverviewBinding binding;

    FastItemAdapter<AssetItem> adapter;
    List<AssetItem> items;

    GridLayoutManager gridLayoutManager;

    public AssetsOverviewController() { super(R.layout.controller_assets_overview); }

    @Override
    public void onViewCreated(View view) {
        binding = ControllerAssetsOverviewBinding.bind(view);

        super.onViewCreated(view, binding.bottomSheet);

        if (assets == null) {
            assets = BehaviorSubject.create();
        }

        if (adapter == null) {
            adapter = new FastItemAdapter<>();
        }

        gridLayoutManager = new GridLayoutManager(view.getContext(), 2) {
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                binding.assetsOverviewProgress.setVisibility(View.INVISIBLE);
            }
        };

        var spacingInPixels = Objects.requireNonNull(getResources()).getDimensionPixelSize(R.dimen.padding_16);
        binding.assetsOverviewRecycler.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, false));

        binding.assetsOverviewRecycler.setNestedScrollingEnabled(false);
        binding.assetsOverviewRecycler.setHasFixedSize(true);
        binding.assetsOverviewRecycler.setLayoutManager(gridLayoutManager);
        binding.assetsOverviewRecycler.setAdapter(adapter);

        RxView.layoutChanges(binding.assetsOverviewRecycler)
                .skip(1)
                .doOnNext(u -> getBehavior().setMaxHeight(binding.assetsOverviewRecycler.getMeasuredHeight()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        invalidate();
    }

    private void invalidate() {
        if (assetsSubscription == null) {
            assetsSubscription = assets
                    .map(assets -> assets
                            .stream()
                            .collect(Collectors.groupingByConcurrent(asset -> asset.path().stream().findFirst().orElse("other")))
                            .values()
                            .stream()
                            .map(groupAssets -> {
                                var mainAssets = groupAssets
                                        .stream()
                                        .filter(asset -> asset.path().size() == 1)
                                        .findFirst()
                                        .orElse(null);

                                if (mainAssets == null) return null;

                                return new AssetItem()
                                        .withIconId(mainAssets.attributes().toIconResource())
                                        .withName(mainAssets.name())
                                        .withIdentifier(assets.indexOf(mainAssets))
                                        .withDescription(String.format("%s assets", groupAssets.size()));
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()))
                    .doOnNext(items -> {
                        if (items != null) {
                            this.items = items;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(adapter::set)
                    .to(autoDisposable(getScopeProvider()));
        }

        if (items != null) {
            adapter.set(items);
            return;
        }

        assetsSubscription.subscribe();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        mapboxMap.setCameraPosition(new CameraPosition.Builder()
                .target(MapUtils.DEFAULT_CENTER)
                .zoom(15.0)
                .build());
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }

    @Override
    public void setAssets(List<Asset<AssetAttribute>> assets) {
        this.assets.onNext(assets);
    }
}
