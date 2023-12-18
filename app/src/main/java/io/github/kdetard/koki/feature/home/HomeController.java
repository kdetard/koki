package io.github.kdetard.koki.feature.home;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsCompat;
import androidx.datastore.rxjava3.RxDataStore;

import com.jakewharton.rxbinding4.widget.RxCompoundButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import autodispose2.FlowableSubscribeProxy;
import autodispose2.MaybeSubscribeProxy;
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
import io.github.kdetard.koki.feature.monitoring.WeatherAttributes;
import io.github.kdetard.koki.misc.Direction;
import io.github.kdetard.koki.openmeteo.OpenMeteoService;
import io.github.kdetard.koki.openmeteo.models.OpenMeteoResponse;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;
import io.github.kdetard.koki.openremote.models.WeatherAsset;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class HomeController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface HomeEntryPoint {
        RxDataStore<Settings> settings();
        OpenRemoteService openRemoteService();
        AqicnService aqicnService();
        OpenMeteoService openMeteoService();
    }

    ControllerHomeBinding binding;

    HomeEntryPoint entryPoint;

    MaybeSubscribeProxy<Asset<AssetAttribute>> weatherSubcribeProxy;

    MaybeSubscribeProxy<Asset<AssetAttribute>> aqicnSubscribeProxy;

    FlowableSubscribeProxy<AqicnResponse> aqicnSubscribeProxyCustom;

    MaybeSubscribeProxy<OpenMeteoResponse> openMeteoSubscribeProxy;

    boolean useCustomProvider = false;

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

        binding.homeWindCard.itemIndexTitle.setText(getApplicationContext().getString(R.string.wind_title));
        binding.homeWindCard.itemIndexIcon.setImageResource(R.drawable.ic_wind_power_24dp);
        binding.homeRainCard.itemIndexInfo.setBackgroundResource(R.drawable.bg_base_card);

        binding.homeWindDirectionCard.itemIndexTitle.setText(getApplicationContext().getString(R.string.windDirection_title));
        binding.homeWindDirectionCard.itemIndexIcon.setImageResource(R.drawable.ic_wind_power_24dp);
        binding.homeRainCard.itemIndexInfo.setBackgroundResource(R.drawable.bg_base_card);

        binding.homeProviderToggle.setChecked(useCustomProvider);

        RxCompoundButton.checkedChanges(binding.homeProviderToggle)
                .skipInitialValue()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(checked -> {
                    useCustomProvider = checked;
                    invalidate();
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        Insetter.builder()
                .padding(WindowInsetsCompat.Type.statusBars())
                .applyToView(binding.homeAppbar.homeAppbarContainer);

        binding.homeSwipeRefresh.setOnRefreshListener(this::invalidate);

        binding.homeSwipeRefresh.setRefreshing(true);

        invalidate();
    }

    private void invalidate() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault());
        binding.homeAppbar.homeWeekday.setText(formatter.format(dayOfWeek));

        binding.homeAppbar.homeTemperatureDetail.setText("...");
        binding.homeAqCard.itemIndexDescription.setText("...");
        binding.homeRainCard.itemIndexDescription.setText("...");
        binding.homeHumidityCard.itemIndexDescription.setText("...");
        binding.homeWindCard.itemIndexDescription.setText("...");
        binding.homeWindDirectionCard.itemIndexDescription.setText("...");

        if (useCustomProvider) {
            if (aqicnSubscribeProxyCustom == null) {
                aqicnSubscribeProxyCustom = entryPoint.settings()
                        .data()
                        .map(Settings::getAqicnToken)
                        .map(token -> token == null || token.isEmpty() ? "667e41e2d287e249d41093d8c589f5d2184cc458" : token)
                        .flatMapSingle(token -> entryPoint.aqicnService().fromCityOrStationId("A37081", token))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(aqicnResponse -> {
                            var api_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.aqi_detail);
                            binding.homeAqCard.itemIndexDescription.setText(String.format(Locale.getDefault(), api_detail, (int) aqicnResponse.data().aqi()));
                            binding.homeSwipeRefresh.setRefreshing(false);
                        })
                        .to(autoDisposable(getScopeProvider()));
            }

            if (openMeteoSubscribeProxy == null)
                openMeteoSubscribeProxy = entryPoint.openMeteoService().getWeather()
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorComplete(throwable -> {
                            Toast.makeText(getApplicationContext(),
                                    String.format(Objects.requireNonNull(getApplicationContext()).getString(R.string.weather_fail), throwable.getMessage()), Toast.LENGTH_SHORT).show();
                            return true;
                        })
                        .doOnSuccess(openMeteoResponse -> {
                            var temperature_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.temperature_detail_custom);
                            var rain_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.rain_detail_custom);
                            var humidity_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.humidity_detail_custom);
                            binding.homeAppbar.homeTemperatureIndex.setText(String.format(Locale.getDefault(), "%.1f%s", openMeteoResponse.current().temperature2m(), openMeteoResponse.currentUnits().temperature2m()));
                            binding.homeAppbar.homeTemperatureDetail.setText(String.format(Locale.getDefault(), temperature_detail, openMeteoResponse.current().apparentTemperature(), openMeteoResponse.currentUnits().apparentTemperature()));
                            binding.homeRainCard.itemIndexDescription.setText(String.format(Locale.getDefault(), rain_detail, (int) openMeteoResponse.current().rain(), openMeteoResponse.currentUnits().rain(), openMeteoResponse.current().interval() / 60));
                            binding.homeHumidityCard.itemIndexDescription.setText(String.format(Locale.getDefault(), humidity_detail, (int) openMeteoResponse.current().relativeHumidity2m(), openMeteoResponse.currentUnits().relativeHumidity2m()));
                            binding.homeSwipeRefresh.setRefreshing(false);
                        })
                        .to(autoDisposable(getScopeProvider()));

            aqicnSubscribeProxyCustom.subscribe();
            openMeteoSubscribeProxy.subscribe();
            return;
        }

        if (aqicnSubscribeProxy == null) {
            weatherSubcribeProxy = entryPoint.openRemoteService()
                    .getAsset("5zI6XqkQVSfdgOrZ1MyWEf")
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorComplete(throwable -> {
                        Toast.makeText(getApplicationContext(),
                                String.format(Objects.requireNonNull(getApplicationContext()).getString(R.string.weather_fail), throwable.getMessage()), Toast.LENGTH_SHORT).show();
                        return true;
                    })
                    .doOnSuccess(asset -> {
                        var weatherAttributes = (WeatherAsset.Attributes) asset.attributes();
                        var place = weatherAttributes.place().value();
                        var temperature = weatherAttributes.temperature().value();
                        var rainfall = weatherAttributes.rainfall().value();
                        var humidity = weatherAttributes.humidity().value();
                        var windSpeed = weatherAttributes.windSpeed().value();
                        var windDirectionDegress = weatherAttributes.windDirection().value();
                        var windDirection = Direction.values()[(int) Math.floor((windDirectionDegress + 22.5) / 45) % 8].getText(Objects.requireNonNull(getApplicationContext()));

                        var temperature_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.temperature_detail);
                        var rain_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.rain_detail);
                        var humidity_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.humidity_detail);
                        var wind_detail = getApplicationContext().getString(R.string.wind_detail);
                        var windDirection_detail = getApplicationContext().getString(R.string.windDirection_detail);

                        binding.homeAppbar.homeTemperatureIndex.setText(String.format(Locale.getDefault(), "%.1f °C", temperature == null ? 0 : temperature));
                        binding.homeAppbar.homeTemperatureDetail.setText(String.format(Locale.getDefault(), temperature_detail, place));
                        binding.homeRainCard.itemIndexDescription.setText(String.format(Locale.getDefault(), rain_detail, rainfall == null ? 0 : rainfall));
                        binding.homeHumidityCard.itemIndexDescription.setText(String.format(Locale.getDefault(), humidity_detail, humidity == null ? "unknown" : humidity));
                        binding.homeWindCard.itemIndexDescription.setText(String.format(Locale.getDefault(), wind_detail, windSpeed == null ? 0 : windSpeed));
                        binding.homeWindDirectionCard.itemIndexDescription.setText(String.format(Locale.getDefault(), windDirection_detail, windDirection));
                        binding.homeSwipeRefresh.setRefreshing(false);
                    })
                    .to(autoDisposable(getScopeProvider()));
        }

        if (aqicnSubscribeProxy == null) {
            aqicnSubscribeProxy = entryPoint.openRemoteService()
                    .getAsset("6Wo9Lv1Oa1zQleuRVfADP4")
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorComplete(throwable -> {
                        Toast.makeText(getApplicationContext(),
                                String.format(Objects.requireNonNull(getApplicationContext()).getString(R.string.weather_fail), throwable.getMessage()), Toast.LENGTH_SHORT).show();
                        return true;
                    })
                    .doOnSuccess(asset -> {
                        var weatherAttributes = (WeatherAsset.Attributes) asset.attributes();
                        var aqi = weatherAttributes.aqiIndex().value();
                        var pm25 = weatherAttributes.pm25Index().value();
                        var pm10 = weatherAttributes.pm10Index().value();
                        var o3 = weatherAttributes.ozoneIndex().value();
                        var no2 = weatherAttributes.no2Index().value();
                        var so2 = weatherAttributes.so2Index().value();
                        var co2 = weatherAttributes.co2Index().value();

                        var aqi_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.aqi_detail);
                        var pm25_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.pm25);
                        var pm10_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.pm10);
                        var o3_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.o3);
                        var no2_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.no2);
                        var so2_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.so2);
                        var co2_detail = Objects.requireNonNull(getApplicationContext()).getString(R.string.co2);

                        binding.homeAqCard.itemIndexDescription.setText(String.format(Locale.getDefault(), aqi_detail, aqi == null ? "unknown" : aqi));
                        binding.homeAqCard.itemPm10.setText(String.format(Locale.getDefault(), pm25_detail, aqi == null ? 0 : pm25));
                        binding.homeAqCard.itemPm25.setText(String.format(Locale.getDefault(), pm10_detail, aqi == null ? 0 : pm10));
                        binding.homeAqCard.itemO3.setText(String.format(Locale.getDefault(), o3_detail, aqi == null ? 0 : o3));
                        binding.homeAqCard.itemNo2.setText(String.format(Locale.getDefault(), no2_detail, aqi == null ? 0 : no2));
                        binding.homeAqCard.itemSo2.setText(String.format(Locale.getDefault(), so2_detail, aqi == null ? 0 : so2));
                        binding.homeAqCard.itemCo2.setText(String.format(Locale.getDefault(), co2_detail, aqi == null ? 0 : co2));
                        binding.homeSwipeRefresh.setRefreshing(false);
                    })
                    .to(autoDisposable(getScopeProvider()));
        }


        weatherSubcribeProxy.subscribe();
        aqicnSubscribeProxy.subscribe();
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
        toolbar.inflateMenu(R.menu.home);
    }
}
