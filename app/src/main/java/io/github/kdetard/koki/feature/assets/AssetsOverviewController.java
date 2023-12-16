package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding4.view.RxView;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    final BehaviorSubject<List<Asset<AssetAttribute>>> assetsSubject;

    ControllerAssetsOverviewBinding binding;

    FastItemAdapter<AssetItem> adapter;
    ArrayList<AssetItem> items;

    GridLayoutManager gridLayoutManager;

    public AssetsOverviewController() {
        super(R.layout.controller_assets_overview);
        assetsSubject = BehaviorSubject.create();
    }

    @Override
    public void onViewCreated(View view) {
        binding = ControllerAssetsOverviewBinding.bind(view);

        super.onViewCreated(view, binding.bottomSheet);

        if (adapter == null)
            adapter = new FastItemAdapter<>();

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
                .doOnNext(u -> getBehavior().setMaxHeight(binding.assetsOverviewRecycler.getMeasuredHeight()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        if (items != null) {
            setAdapterItems(null);
            return;
        }

        assetsSubject
                .map(assets -> {
                    var items = new ArrayList<AssetItem>();

                    assets
                            .stream()
                            .collect(Collectors.groupingByConcurrent(asset -> asset.path().stream().findFirst().orElse("other")))
                            .forEach((group, groupAssets) -> {
                                var mainAssets = groupAssets
                                        .stream()
                                        .filter(asset -> asset.path().size() == 1)
                                        .findFirst()
                                        .orElse(null);

                                if (mainAssets == null) return;

                                var item = new AssetItem()
                                        .withIconId(mainAssets.attributes().toIconResource())
                                        .withName(mainAssets.name())
                                        .withIdentifier(assets.indexOf(mainAssets))
                                        .withDescription(String.format("%s assets", groupAssets.size()));

                                items.add(item);
                            });

                    return items;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::setAdapterItems)
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

    }

    private void setAdapterItems(ArrayList<AssetItem> items) {
        if (items != null) this.items = items;
        adapter.set(this.items);
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
    public void setAssets(List<Asset<AssetAttribute>> assetsSubject) {
        this.assetsSubject.onNext(assetsSubject);
    }
}
