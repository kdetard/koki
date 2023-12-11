package io.github.kdetard.koki.feature.monitoring;

import static com.patrykandpatrick.vico.core.entry.EntryListExtensionsKt.entryModelOf;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.patrykandpatrick.vico.core.entry.FloatEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import dev.chrisbanes.insetter.Insetter;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerMonitoringBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.misc.EnumUtils;
import io.github.kdetard.koki.misc.Tuple;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.github.kdetard.koki.openremote.models.DatapointQuery;
import io.reactivex.rxjava3.core.Observable;

public class MonitoringController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface MonitoringEntryPoint {
        OpenRemoteService service();
    }

    MonitoringEntryPoint entryPoint;
    ControllerMonitoringBinding binding;
    MaterialDatePicker<Long> datePicker;

    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
    SimpleDateFormat chartTimeFormat = dateFormat;

    public MonitoringController() { super(R.layout.controller_monitoring); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        final Map<String, WeatherAttributes> weatherAttributesStringMap = EnumUtils.toEnumMap(WeatherAttributes.values(),
                a -> a.getText(view.getContext()));

        final List<WeatherAttributes> weatherAttributesList = weatherAttributesStringMap.values().stream().collect(Collectors.toList());

        final Map<String, TimeFrameOptions> timeFrameOptionsStringMap = EnumUtils.toEnumMap(TimeFrameOptions.values(),
                a -> a.getText(view.getContext()));

        final List<TimeFrameOptions> timeFrameOptionsList = timeFrameOptionsStringMap.values().stream().collect(Collectors.toList());

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), MonitoringEntryPoint.class);

        binding = ControllerMonitoringBinding.bind(view);

        Insetter.builder()
                .padding(WindowInsetsCompat.Type.statusBars())
                .applyToView(binding.getRoot());

        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select ending date")
                .setTextInputFormat(dateFormat)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(this::setEndingDate);

        RxView
                .clicks(binding.monitorEnding)
                .to(autoDisposable(getScopeProvider()))
                .subscribe(unit -> {
                    datePicker.show(((FragmentActivity) Objects.requireNonNull(getActivity())).getSupportFragmentManager(), "");
                });

        binding.monitorAttribute.setSimpleItems(weatherAttributesStringMap.keySet().stream().toArray(String[]::new));
        binding.monitorTimeFrame.setSimpleItems(timeFrameOptionsStringMap.keySet().stream().toArray(String[]::new));

        Observable.combineLatest(
            RxAutoCompleteTextView.itemClickEvents(binding.monitorAttribute),
            RxAutoCompleteTextView.itemClickEvents(binding.monitorTimeFrame),
            RxTextView.textChanges(binding.monitorEnding).skipInitialValue(),
            (attributeEvent, timeFrameEvent, charSequence) -> new Tuple<>(
                weatherAttributesList.get((int) attributeEvent.getId()),
                timeFrameOptionsList.get((int) timeFrameEvent.getId()),
                charSequence.toString()
            )
        )
            .flatMapSingle(tuple -> {
                var weatherAttribute = tuple.first().getOpenRemoteString();

                var toTimeFrameDate = dateFormat.parse(tuple.third());

                var toTimeFrame = System.currentTimeMillis();

                if (toTimeFrameDate != null) {
                    toTimeFrame = toTimeFrameDate.getTime();
                }

                chartTimeFormat = switch (tuple.second()) {
                    case HOUR, DAY, WEEK -> new SimpleDateFormat("HH:mm");
                    case MONTH -> new SimpleDateFormat("dd/MM");
                    case YEAR -> new SimpleDateFormat("MMM yyyy");
                };

                var fromTimeFrame = toTimeFrame - tuple.second().toMillis();

                var datapointQuery = new DatapointQuery(100, fromTimeFrame, toTimeFrame, DatapointQuery.DEFAULT_TYPE);

                return entryPoint.service().getDatapoint("5zI6XqkQVSfdgOrZ1MyWEf", weatherAttribute, datapointQuery);
            })
            .doOnError(throwable ->
                    Toast.makeText(view.getContext(), String.format("An error occurred %s", throwable.getMessage()), Toast.LENGTH_LONG).show())
            .onErrorComplete()
            .doOnNext(datapoints -> {
                binding.monitorChart.setModel(
                        entryModelOf(datapoints.stream()
                                .map(d -> new FloatEntry(d.timestamp(), d.value()))
                                .collect(Collectors.toList()))
                );
                binding.monitorChart.animate();
            })
            .to(autoDisposable(getScopeProvider()))
            .subscribe();
    }

    private void setEndingDate(long selection) {
        binding.monitorEnding.setText(dateFormat.format(new Date(selection)));
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);
        datePicker.removeOnPositiveButtonClickListener(this::setEndingDate);
        datePicker = null;
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }
}