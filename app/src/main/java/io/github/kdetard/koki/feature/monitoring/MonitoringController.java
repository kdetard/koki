package io.github.kdetard.koki.feature.monitoring;

import static com.patrykandpatrick.vico.core.entry.EntryListExtensionsKt.entryModelOf;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.jakewharton.rxbinding4.view.RxView;
import com.jakewharton.rxbinding4.widget.RxAutoCompleteTextView;
import com.jakewharton.rxbinding4.widget.RxTextView;
import com.patrykandpatrick.vico.core.axis.AxisPosition;
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter;
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis;
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis;
import com.patrykandpatrick.vico.core.chart.values.ChartValues;
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

    ChartEntryModelProducer chartEntryModelProducer;
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

        if (startAxis != null) {
            binding.monitorChart.setStartAxis(startAxis);
        }

        if (bottomAxis != null) {
            binding.monitorChart.setBottomAxis(bottomAxis);
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
                    invalidate();
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        RxAutoCompleteTextView.itemClickEvents(binding.monitorTimeFrame)
                .doOnNext(timeFrameEvent -> {
                    timeFrameOption = Arrays.stream(TimeFrameOptions.values()).skip(timeFrameEvent.getId()).findFirst().orElse(null);
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
    }

    private void invalidate() {
        if (weatherAttribute == null || timeFrameOption == null || endingDate == null || endingDate.isEmpty()) return;

        binding.monitorAttribute.setEnabled(false);
        binding.monitorTimeFrame.setEnabled(false);
        binding.monitorEnding.setEnabled(false);
        binding.monitorSwipeRefresh.setRefreshing(true);

        var openRemoteString = weatherAttribute.getOpenRemoteString();

        Date toTimeFrameDate;

        try {
            toTimeFrameDate = dateFormat.parse(endingDate);
        } catch (ParseException e) {
            Toast.makeText(getApplicationContext(), R.string.invalid_date, Toast.LENGTH_LONG).show();
            binding.monitorAttribute.setEnabled(true);
            binding.monitorTimeFrame.setEnabled(true);
            binding.monitorEnding.setEnabled(true);
            binding.monitorSwipeRefresh.setRefreshing(false);
            return;
        }

        var toTimeFrame = System.currentTimeMillis();

        if (toTimeFrameDate != null) {
            toTimeFrame = toTimeFrameDate.getTime();
        }

        chartTimeFormat = new SimpleDateFormat(switch (timeFrameOption) {
            case HOUR, DAY, WEEK -> "HH:mm";
            case MONTH -> "dd/MM";
            case YEAR -> "MMM yy";
        }, Locale.getDefault());

        var fromTimeFrame = toTimeFrame - timeFrameOption.toMillis();

        var datapointQuery = new DatapointQuery(100, fromTimeFrame, toTimeFrame, DatapointQuery.DEFAULT_TYPE);

        entryPoint.service()
            .getDatapoint("5zI6XqkQVSfdgOrZ1MyWEf", openRemoteString, datapointQuery)
            .doOnSuccess(datapoints -> {
                startAxis = (VerticalAxis<AxisPosition.Vertical.Start>) binding.monitorChart.getStartAxis();
                assert startAxis != null;
                startAxis.setHorizontalLabelPosition(VerticalAxis.HorizontalLabelPosition.Inside);

                bottomAxis = (HorizontalAxis<AxisPosition.Horizontal.Bottom>) binding.monitorChart.getBottomAxis();
                assert bottomAxis != null;
                bottomAxis.setLabelRotationDegrees(-75f);

                if (datapoints.size() < 2)
                    return;

                var modifier = switch (timeFrameOption) {
                    case HOUR, DAY -> 1;
                    case WEEK -> 4;
                    case MONTH -> 24;
                    case YEAR -> 7 * 24;
                };

                var timestamps = datapoints.stream()
                        .map(Datapoint::timestamp)
                        .collect(Collectors.toList());

                var data = IntStream.range(0, datapoints.size())
                        .mapToObj(i -> new FloatEntry(i, datapoints.get(i).value()))
                        .collect(Collectors.toList());

                chartEntryModel = entryModelOf(data);

                var horizontalAxisValueFormatter = new AxisValueFormatter<AxisPosition.Horizontal.Bottom>() {
                    @NonNull
                    @Override
                    public CharSequence formatValue(float timestamp, @NonNull ChartValues _chartValues) {
                        if (timestamp % modifier != 0) return "";
                        return chartTimeFormat.format(timestamps.get((int) timestamp));
                    }
                };

                bottomAxis.setValueFormatter(horizontalAxisValueFormatter);
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(throwable -> {
                Timber.d(throwable, "An error occurred");
                Toast.makeText(getApplicationContext(), String.format("An error occurred: %s", throwable.getMessage()), Toast.LENGTH_LONG).show();
                binding.monitorSwipeRefresh.setRefreshing(false);
                binding.monitorAttribute.setEnabled(true);
                binding.monitorTimeFrame.setEnabled(true);
                binding.monitorEnding.setEnabled(true);
            })
            .onErrorResumeNext(throwable -> Single.just(Collections.emptyList()))
            .doOnSuccess(datapoints -> {
                if (datapoints.size() < 2)
                    Toast.makeText(getApplicationContext(), R.string.datapoints_not_enough, Toast.LENGTH_LONG).show();

                binding.monitorChart.setStartAxis(startAxis);
                binding.monitorChart.setBottomAxis(bottomAxis);

                if (chartEntryModel != null) {
                    if (chartEntryModelProducer == null) {
                        chartEntryModelProducer = new ChartEntryModelProducer(chartEntryModel.getEntries(), Dispatchers.getDefault());
                        binding.monitorChart.setEntryProducer(chartEntryModelProducer);
                    } else {
                        chartEntryModelProducer.setEntries(chartEntryModel.getEntries(), mutableExtraStore -> null);
                    }
                }

                binding.monitorSwipeRefresh.setRefreshing(false);
                binding.monitorAttribute.setEnabled(true);
                binding.monitorTimeFrame.setEnabled(true);
                binding.monitorEnding.setEnabled(true);
            })
            .to(autoDisposable(getScopeProvider()))
            .subscribe();
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
        if (!Objects.requireNonNull(getActivity()).isChangingConfigurations()) {
            datePicker.removeOnPositiveButtonClickListener(this::setEndingDate);
            datePicker = null;
            timePicker.removeOnPositiveButtonClickListener(this::setTime);
            timePicker = null;
        }
        super.onDestroyView(view);
    }

    @Override
    public void configureMenu(Toolbar toolbar) {
        super.configureMenu(toolbar);
    }
}