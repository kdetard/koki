package io.github.kdetard.koki.feature.monitoring;

import static com.patrykandpatrick.vico.core.entry.EntryListExtensionsKt.entryModelOf;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.patrykandpatrick.vico.core.entry.FloatEntry;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
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
import io.github.kdetard.koki.misc.Tuple;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.github.kdetard.koki.openremote.models.DatapointQuery;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
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
    MaterialTimePicker timePicker;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
    SimpleDateFormat chartTimeFormat;
    long endingDateMillis;
    long timeMillis;

    public MonitoringController() { super(R.layout.controller_monitoring); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

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

        timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                .setTitleText("Select hour of day")
                .build();

        timePicker.addOnPositiveButtonClickListener(this::setTime);
        timePicker.addOnCancelListener(this::setDefaultTime);
        timePicker.addOnNegativeButtonClickListener(this::setDefaultTime);
        datePicker.addOnPositiveButtonClickListener(this::setEndingDate);
        datePicker.addOnCancelListener(this::setDefaultEndingDate);
        datePicker.addOnNegativeButtonClickListener(this::setDefaultEndingDate);

        RxView
                .clicks(binding.monitorEnding)
                .to(autoDisposable(getScopeProvider()))
                .subscribe(unit -> datePicker.show(getSupportFragmentManager() , ""));

        binding.monitorAttribute.setSimpleItems(Arrays.stream(WeatherAttributes.values()).map(a -> a.getText(view.getContext())).toArray(String[]::new));
        binding.monitorTimeFrame.setSimpleItems(Arrays.stream(TimeFrameOptions.values()).map(a -> a.getText(view.getContext())).toArray(String[]::new));

        Observable.combineLatest(
            RxAutoCompleteTextView.itemClickEvents(binding.monitorAttribute),
            RxAutoCompleteTextView.itemClickEvents(binding.monitorTimeFrame),
            RxTextView.textChanges(binding.monitorEnding).skipInitialValue(),
            (attributeEvent, timeFrameEvent, charSequence) -> new Tuple<>(
                Arrays.stream(WeatherAttributes.values()).skip(attributeEvent.getId()).findFirst().orElse(null),
                Arrays.stream(TimeFrameOptions.values()).skip(timeFrameEvent.getId()).findFirst().orElse(null),
                charSequence.toString()
            )
        )
            .flatMapSingle(tuple -> {
                var weatherAttribute = tuple.first().getOpenRemoteString();
                var timeFrame = tuple.second();

                var toTimeFrameDate = dateFormat.parse(tuple.third());

                var toTimeFrame = System.currentTimeMillis();

                if (toTimeFrameDate != null) {
                    toTimeFrame = toTimeFrameDate.getTime();
                }

                chartTimeFormat = new SimpleDateFormat(switch (tuple.second()) {
                    case HOUR, DAY, WEEK -> "HH:mm";
                    case MONTH -> "dd/MM";
                    case YEAR -> "MMM yyyy";
                }, Locale.getDefault());

                var fromTimeFrame = toTimeFrame - timeFrame.toMillis();

                var datapointQuery = new DatapointQuery(100, fromTimeFrame, toTimeFrame, DatapointQuery.DEFAULT_TYPE);

                return entryPoint.service().getDatapoint("5zI6XqkQVSfdgOrZ1MyWEf", weatherAttribute, datapointQuery);
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(throwable ->
                    Toast.makeText(view.getContext(), String.format("An error occurred %s", throwable.getMessage()), Toast.LENGTH_LONG).show())
            .onErrorComplete()
            .doOnNext(datapoints -> {
                if (datapoints.size() < 2) {
                    Toast.makeText(view.getContext(), "Not enough datapoints", Toast.LENGTH_LONG).show();
                    return;
                }
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

    private<T> void setDefaultEndingDate(T listener) {
        if (endingDateMillis != 0) return;
        endingDateMillis = Calendar.getInstance().getTimeInMillis();
        timePicker.show(getSupportFragmentManager(), "");
    }

    private void setEndingDate(long selection) {
        endingDateMillis = selection - Calendar.getInstance().getTimeZone().getRawOffset();
        timePicker.show(getSupportFragmentManager(), "");
    }

    private<T> void setDefaultTime(T listener) {
        if (timeMillis != 0) return;
        timeMillis = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000L + Calendar.getInstance().get(Calendar.MINUTE) * 60 * 1000L;
        binding.monitorEnding.setText(dateFormat.format(endingDateMillis + timeMillis));
    }

    private<T> void setTime(T listener) {
        timeMillis = timePicker.getHour() * 60 * 60 * 1000L + timePicker.getMinute() * 60 * 1000L;
        binding.monitorEnding.setText(dateFormat.format(endingDateMillis + timeMillis));
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