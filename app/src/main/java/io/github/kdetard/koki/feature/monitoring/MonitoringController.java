package io.github.kdetard.koki.feature.monitoring;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.jakewharton.rxbinding4.view.RxView;

import java.util.Arrays;
import java.util.Objects;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerMonitoringBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.github.kdetard.koki.openremote.models.DatapointQuery;
import timber.log.Timber;

public class MonitoringController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface MonitoringEntryPoint {
        OpenRemoteService service();
    }

    MonitoringEntryPoint entryPoint;
    ControllerMonitoringBinding binding;
    MaterialDatePicker<Long> datePicker;

    public MonitoringController() { super(R.layout.controller_monitoring); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), MonitoringEntryPoint.class);

        binding = ControllerMonitoringBinding.bind(view);
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select ending date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        /*RxView.clicks(binding.monitorEnding)
                .doOnNext(u -> datePicker.show(getParentFragmentManager(), "ending"))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();*/

        binding.monitorAttribute.setSimpleItems(Arrays.stream(WeatherAttributes.values()).map(a -> a.getText(getApplicationContext())).toArray(String[]::new));
        binding.monitorTimeFrame.setSimpleItems(Arrays.stream(TimeFrameOptions.values()).map(t -> t.getText(getApplicationContext())).toArray(String[]::new));

        entryPoint.service().getDatapoint("5zI6XqkQVSfdgOrZ1MyWEf", "temperature", new DatapointQuery(100, 1702141200000L, 1702227600000L, DatapointQuery.DEFAULT_TYPE))
                .doOnSuccess(d -> {
                    Timber.d("Datapoint %s", d);
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }
}