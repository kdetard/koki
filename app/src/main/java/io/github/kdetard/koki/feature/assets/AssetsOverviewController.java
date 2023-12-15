package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;

import com.jakewharton.rxbinding4.view.RxView;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerAssetsOverviewBinding;
import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class AssetsOverviewController extends BottomSheetController implements OnAssetsAvailableListener {
    final PublishSubject<List<Asset<AssetAttribute>>> assetsSubject;

    ControllerAssetsOverviewBinding binding;

    FastItemAdapter<AssetItem> adapter;

    GridLayoutManager gridLayoutManager;

    public AssetsOverviewController() {
        super(R.layout.controller_assets_overview);
        assetsSubject = PublishSubject.create();
    }

    @Override
    public void onViewCreated(View view) {
        binding = ControllerAssetsOverviewBinding.bind(view);

        super.onViewCreated(view, binding.bottomSheet);

        adapter = new FastItemAdapter<>();

        gridLayoutManager = new GridLayoutManager(view.getContext(), 2);

        binding.assetsOverviewRecycler.setNestedScrollingEnabled(false);
        binding.assetsOverviewRecycler.setHasFixedSize(true);
        binding.assetsOverviewRecycler.setLayoutManager(gridLayoutManager);
        binding.assetsOverviewRecycler.setAdapter(adapter);

        RxView.layoutChanges(binding.assetsOverviewRecycler)
                .doOnNext(u -> getBehavior().setMaxHeight(binding.assetsOverviewRecycler.getMeasuredHeight()))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

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
                .doOnNext(adapter::set)
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
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
