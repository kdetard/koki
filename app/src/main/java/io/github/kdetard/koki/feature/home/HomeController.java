package io.github.kdetard.koki.feature.home;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.appcompat.widget.Toolbar;

import java.util.Objects;
import java.util.stream.Collectors;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerHomeBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.ConsoleAsset;
import io.github.kdetard.koki.openremote.models.GroupAsset;
import io.github.kdetard.koki.openremote.models.HTTPAgentAsset;
import io.github.kdetard.koki.openremote.models.LightAsset;
import io.github.kdetard.koki.openremote.models.MQTTAgentAsset;
import io.github.kdetard.koki.openremote.models.WeatherAsset;
import timber.log.Timber;

public class HomeController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface HomeEntryPoint {
        OpenRemoteService service();
    }

    ControllerHomeBinding binding;
    HomeEntryPoint entryPoint;

    public HomeController() {
        super(R.layout.controller_home);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), HomeEntryPoint.class);
        binding = ControllerHomeBinding.bind(view);

        entryPoint.service().getAssets()
                .map(assets -> assets.stream()
                        .filter(asset -> asset.attributes().location() != null && asset.attributes().location().value() != null)
                        .collect(Collectors.toList()))
                .doOnSuccess(assets -> {
                    Timber.d("Assets: %s", assets.size());
                    for (var asset : assets) {
                        Timber.d("Asset id: %s", asset.id());
                        Timber.d("Asset type: %s", asset.type());
                        Timber.d("Asset location: %s", asset.attributes().location().value());
                        Timber.d("Asset name: %s", asset.name());
                    }
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        entryPoint.service().getDashboards()
                .doOnSuccess(dashboards -> {
                    Timber.d("Dashboards: %s", dashboards.size());
                    for (var dashboard : dashboards) {
                        Timber.d("Dashboard name: %s", dashboard.displayName());
                        if (dashboard.template().widgets() != null) {
                            dashboard.template().widgets()
                                    .stream()
                                    .findFirst()
                                    .map(widget -> {
                                        Timber.d("Widget type: %s", widget.type());
                                        return widget;
                                    });
                        }
                    }
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
        toolbar.inflateMenu(R.menu.home);
    }
}
