package io.github.kdetard.koki.feature.monitoring;

import static com.patrykandpatrick.vico.core.entry.EntryListExtensionsKt.entryModelOf;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.patrykandpatrick.vico.core.axis.AxisPosition;
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis;
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis;
import com.patrykandpatrick.vico.core.entry.ChartEntryModel;
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer;
import com.patrykandpatrick.vico.core.entry.FloatEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kotlinx.coroutines.Dispatchers;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerMonitoringBinding;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.openremote.OpenRemoteService;
import io.github.kdetard.koki.openremote.models.Datapoint;
import io.github.kdetard.koki.openremote.models.DatapointQuery;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
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
    MaterialTimePicker timePicker;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
    SimpleDateFormat chartTimeFormat;
    long endingDateMillis;
    long timeMillis;

    List<Long> timestamps;
    ChartEntryModelProducer chartEntryModelProducer;
    @Nullable
    ChartEntryModel chartEntryModel;
    HorizontalAxis<AxisPosition.Horizontal.Bottom> bottomAxis;
    VerticalAxis<AxisPosition.Vertical.Start> startAxis;

    WeatherAttributes weatherAttribute;
    TimeFrameOptions timeFrameOption;
    String endingDate;

    public MonitoringController() { super(R.layout.controller_monitoring); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), MonitoringEntryPoint.class);

        binding = ControllerMonitoringBinding.bind(view);

        if (timePicker == null) {
            timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                    .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                    .setTitleText(R.string.hour_selection)
                    .build();
            timePicker.addOnPositiveButtonClickListener(this::setTime);
            timePicker.addOnCancelListener(this::setDefaultTime);
            timePicker.addOnNegativeButtonClickListener(this::setDefaultTime);
        }

        if (datePicker == null) {
            datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.ending_date_selection)
                    .setTextInputFormat(dateFormat)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(this::setEndingDate);
            datePicker.addOnCancelListener(this::setDefaultEndingDate);
            datePicker.addOnNegativeButtonClickListener(this::setDefaultEndingDate);
        }

        if (chartEntryModel != null) {
            binding.monitorChart.setModel(chartEntryModel);
        }

        RxView
                .clicks(binding.monitorEnding)
                .to(autoDisposable(getScopeProvider()))
                .subscribe(unit -> datePicker.show(getSupportFragmentManager(), MonitoringController.class.getSimpleName()));

        RxView.attaches(binding.monitorAttribute)
                .doOnNext(u -> binding.monitorAttribute.setSimpleItems(
                        Arrays.stream(WeatherAttributes.values()).map(a -> a.getText(view.getContext())).toArray(String[]::new)))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxView.attaches(binding.monitorTimeFrame)
                .doOnNext(u -> binding.monitorTimeFrame.setSimpleItems(
                        Arrays.stream(TimeFrameOptions.values()).map(a -> a.getText(view.getContext())).toArray(String[]::new)))
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        binding.monitorSwipeRefresh.setOnRefreshListener(this::invalidate);

        RxAutoCompleteTextView.itemClickEvents(binding.monitorAttribute)
                .doOnNext(attributeEvent -> {
                    weatherAttribute = Arrays.stream(WeatherAttributes.values()).skip(attributeEvent.getId()).findFirst().orElse(null);
                    assert weatherAttribute != null;
                    startAxis.setTitle(weatherAttribute.getText(view.getContext()));
                    invalidate();
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxAutoCompleteTextView.itemClickEvents(binding.monitorTimeFrame)
                .doOnNext(timeFrameEvent -> {
                    timeFrameOption = Arrays.stream(TimeFrameOptions.values()).skip(timeFrameEvent.getId()).findFirst().orElse(null);
                    assert timeFrameOption != null;
                    bottomAxis.setTitle(timeFrameOption.getText(view.getContext()));
                    invalidate();
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxTextView.textChanges(binding.monitorEnding)
                .doOnNext(charSequence -> {
                    endingDate = charSequence.toString();
                    invalidate();
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        binding.monitorChart.setHorizontalScrollEnabled(true);

        startAxis = (VerticalAxis<AxisPosition.Vertical.Start>) binding.monitorChart.getStartAxis();
        assert startAxis != null;
        startAxis.setHorizontalLabelPosition(VerticalAxis.HorizontalLabelPosition.Inside);

        bottomAxis = (HorizontalAxis<AxisPosition.Horizontal.Bottom>) binding.monitorChart.getBottomAxis();
        assert bottomAxis != null;
        bottomAxis.setLabelRotationDegrees(-75f);
    }

    private void invalidate() {
        toggleInput(false);

        if (weatherAttribute == null || timeFrameOption == null || endingDate == null || endingDate.isEmpty()) {
            toggleInput(true);
            return;
        }

        var openRemoteString = weatherAttribute.getOpenRemoteString();

        Date toTimeFrameDate;

        try {
            toTimeFrameDate = dateFormat.parse(endingDate);
        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(), R.string.invalid_date, Toast.LENGTH_LONG).show();
            toggleInput(true);
            return;
        }

        var toTimeFrame = System.currentTimeMillis();

        if (toTimeFrameDate != null) {
            toTimeFrame = toTimeFrameDate.getTime();
        }

        chartTimeFormat = new SimpleDateFormat(switch (timeFrameOption) {
            case HOUR, DAY -> "HH:mm";
            case WEEK, MONTH -> "dd/MM HH:mm";
            case YEAR -> "MMM dd yy, HH:mm";
        }, Locale.getDefault());

        var fromTimeFrame = toTimeFrame - timeFrameOption.toMillis();

        var datapointQuery = new DatapointQuery(100, fromTimeFrame, toTimeFrame, DatapointQuery.DEFAULT_TYPE);

        entryPoint.service()
            .getDatapoint("5zI6XqkQVSfdgOrZ1MyWEf", openRemoteString, datapointQuery)
            .doOnSuccess(datapoints -> {
                if (datapoints.size() < 2)
                    return;

                var data = IntStream.range(0, datapoints.size())
                        .mapToObj(i -> new FloatEntry(i, datapoints.get(i).value()))
                        .collect(Collectors.toList());

                chartEntryModel = entryModelOf(data);

                timestamps = datapoints.stream()
                        .map(Datapoint::timestamp)
                        .collect(Collectors.toList());

                bottomAxis.setValueFormatter((timestamp, _chartValues) ->
                        chartTimeFormat.format(timestamps.get((int) timestamp)));
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(throwable -> {
                Timber.d(throwable, "An error occurred");
                Toast.makeText(getApplicationContext(), String.format("An error occurred: %s", throwable.getMessage()), Toast.LENGTH_LONG).show();
                toggleInput(true);
            })
            .onErrorResumeNext(throwable -> Single.just(Collections.emptyList()))
            .doOnSuccess(datapoints -> {
                if (datapoints.size() < 2)
                    Toast.makeText(getApplicationContext(), R.string.datapoints_not_enough, Toast.LENGTH_SHORT).show();

                if (chartEntryModel != null) {
                    chartEntryModelProducer = (ChartEntryModelProducer)binding.monitorChart.getEntryProducer();

                    if (chartEntryModelProducer == null) {
                        chartEntryModelProducer = new ChartEntryModelProducer(chartEntryModel.getEntries(), Dispatchers.getDefault());
                        binding.monitorChart.setEntryProducer(chartEntryModelProducer);
                    }

                    chartEntryModelProducer.setEntries(chartEntryModel.getEntries(), mutableExtraStore -> null);
                }

                toggleInput(true);
            })
            .to(autoDisposable(getScopeProvider()))
            .subscribe();
    }

    private void toggleInput(boolean predicate) {
        binding.monitorSwipeRefresh.setRefreshing(!predicate);
        binding.monitorAttribute.setEnabled(predicate);
        binding.monitorTimeFrame.setEnabled(predicate);
        binding.monitorEnding.setEnabled(predicate);
    }

    private<T> void setDefaultEndingDate(T listener) {
        if (endingDateMillis != 0) return;
        var startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        endingDateMillis = startOfToday.toEpochSecond() * 1000;
        timePicker.show(getSupportFragmentManager(), MonitoringController.class.getSimpleName());
    }

    private void setEndingDate(long selection) {
        endingDateMillis = selection - Calendar.getInstance().getTimeZone().getRawOffset();
        timePicker.show(getSupportFragmentManager(), MonitoringController.class.getSimpleName());
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
        datePicker.removeOnPositiveButtonClickListener(this::setEndingDate);
        datePicker = null;
        timePicker.removeOnPositiveButtonClickListener(this::setTime);
        timePicker = null;
        super.onDestroyView(view);
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }
}