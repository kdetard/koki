package io.github.kdetard.koki.feature.home;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsCompat;
import androidx.datastore.rxjava3.RxDataStore;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import dev.chrisbanes.insetter.Insetter;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.aqicn.AqicnService;
import io.github.kdetard.koki.aqicn.models.AqicnResponse;
import io.github.kdetard.koki.databinding.ControllerHomeBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.openmeteo.OpenMeteoService;
import io.github.kdetard.koki.openmeteo.models.OpenMeteoResponse;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class HomeController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface HomeEntryPoint {
        RxDataStore<Settings> settings();
        AqicnService aqicnService();
        OpenMeteoService openMeteoService();
    }

    ControllerHomeBinding binding;
    HomeEntryPoint entryPoint;
    Disposable meteoDisposable;
    Disposable aqicnDisposable;

    AqicnResponse aqicnResponse;
    OpenMeteoResponse openMeteoResponse;

    public HomeController() {
        super(R.layout.controller_home);
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), HomeEntryPoint.class);

        binding = ControllerHomeBinding.bind(view);

        binding.homeAppbar.homeGreeting.setText("Hello, user"/* + entryPoint.settings().data().map(Settings::getUsername).blockingGet() + "!""*/);
        binding.homeAppbar.homeWeekday.setText(LocalDate.now().getDayOfWeek().name());

        binding.homeAqCard.itemChartTitle.setText(R.string.air_quality_title);
        binding.homeAqCard.itemChartIcon.setImageResource(R.drawable.ic_aq_24dp);
        binding.homeAqCard.itemChartInfo.setBackgroundResource(R.drawable.bg_base_card);

        binding.homeRainCard.itemIndexTitle.setText(R.string.rain_fall_title);
        binding.homeRainCard.itemIndexIcon.setImageResource(R.drawable.ic_rainy_24dp);
        binding.homeRainCard.itemIndexInfo.setBackgroundResource(R.drawable.bg_base_card);

        binding.homeHumidityCard.itemIndexTitle.setText(R.string.humidity_title);
        binding.homeHumidityCard.itemIndexIcon.setImageResource(R.drawable.ic_humidity_percentage_24dp);
        binding.homeHumidityCard.itemIndexInfo.setBackgroundResource(R.drawable.bg_humidity_card);

        Insetter.builder()
                .padding(WindowInsetsCompat.Type.statusBars())
                .applyToView(binding.homeAppbar.homeAppbarContainer);

        binding.homeSwipeRefresh.setOnRefreshListener(this::invalidate);

        if (aqicnResponse != null && openMeteoResponse != null) {
            setAqicnState(null);
            setOpenMeteoState(null);
            return;
        }

        binding.homeSwipeRefresh.setRefreshing(true);
        invalidate();
    }

    private void invalidate() {
        if (aqicnDisposable != null) {
            aqicnDisposable.dispose();
            aqicnDisposable = null;
        }

        if (meteoDisposable != null) {
            meteoDisposable.dispose();
            meteoDisposable = null;
        }

        binding.homeAppbar.homeWeekday.setText(LocalDate.now().getDayOfWeek().name());
        binding.homeAqCard.itemIndexDescription.setText("...");
        binding.homeRainCard.itemIndexDescription.setText("...");
        binding.homeHumidityCard.itemIndexDescription.setText("...");

        aqicnDisposable = entryPoint.settings()
                .data()
                .map(Settings::getAqicnToken)
                .map(token -> token == null || token.isEmpty() ? "667e41e2d287e249d41093d8c589f5d2184cc458" : token)
                .flatMapSingle(token -> entryPoint.aqicnService().fromCityOrStationId("A37081", token))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(_r -> binding.homeSwipeRefresh.setRefreshing(false))
                .doOnNext(this::setAqicnState)
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        meteoDisposable = entryPoint.openMeteoService().getWeather()
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorComplete(throwable -> {
                    // handleError(getApplicationContext(), throwable);
                    return true;
                })
                .doOnSuccess(_r -> binding.homeSwipeRefresh.setRefreshing(false))
                .doOnSuccess(this::setOpenMeteoState)
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
    }

    private void setAqicnState(AqicnResponse response) {
        if (response != null) aqicnResponse = response;
        binding.homeAqCard.itemIndexDescription.setText(String.format(Locale.getDefault(), "Current index is %d", (int) aqicnResponse.data().aqi()));
    }

    private void setOpenMeteoState(OpenMeteoResponse response) {
        if (response != null) openMeteoResponse = response;
        binding.homeAppbar.homeTemperatureIndex.setText(String.format(Locale.getDefault(), "%.1f%s", openMeteoResponse.current().temperature2m(), openMeteoResponse.currentUnits().temperature2m()));
        binding.homeAppbar.homeTemperatureDetail.setText(String.format(Locale.getDefault(), "Feels like %.1f%s", openMeteoResponse.current().apparentTemperature(), openMeteoResponse.currentUnits().apparentTemperature()));
        binding.homeRainCard.itemIndexDescription.setText(String.format(Locale.getDefault(), "%d%s expected in the next %d minutes", (int) openMeteoResponse.current().rain(), openMeteoResponse.currentUnits().rain(), openMeteoResponse.current().interval() / 60));
        binding.homeHumidityCard.itemIndexDescription.setText(String.format(Locale.getDefault(), "The dew point is %d%s right now", (int) openMeteoResponse.current().relativeHumidity2m(), openMeteoResponse.currentUnits().relativeHumidity2m()));
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
        toolbar.inflateMenu(R.menu.home);
    }
}
