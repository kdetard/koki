package io.github.kdetard.koki.feature.monitoring;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.patrykandpatrick.vico.core.axis.AxisItemPlacer;
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions;
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext;
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout;
import com.patrykandpatrick.vico.core.context.MeasureContext;
import static com.patrykandpatrick.vico.core.extension.NumberExtensionsKt.getHalf;
import static com.patrykandpatrick.vico.core.extension.NumberExtensionsKt.getRound;

import java.util.Collections;
import java.util.List;

import kotlin.ranges.ClosedFloatingPointRange;

public class TimestampHorizontalAxisItemPlacer implements AxisItemPlacer.Horizontal {
    private final int spacing;
    private final int offset;
    private final boolean shiftExtremeTicks;
    private final boolean addExtremeLabelPadding;

    public TimestampHorizontalAxisItemPlacer(int spacing,
                                             int offset,
                                             boolean shiftExtremeTicks,
                                             boolean addExtremeLabelPadding) {
        this.spacing = spacing;
        this.offset = offset;
        this.shiftExtremeTicks = shiftExtremeTicks;
        this.addExtremeLabelPadding = addExtremeLabelPadding;
    }

    @Override
    public boolean getShiftExtremeTicks(@NonNull ChartDrawContext chartDrawContext) { return shiftExtremeTicks; }

    @Override
    public boolean getAddFirstLabelPadding(@NonNull MeasureContext context) {
        return context.getHorizontalLayout() instanceof HorizontalLayout.FullWidth && addExtremeLabelPadding && offset == 0;
    }

    @Override
    public boolean getAddLastLabelPadding(@NonNull MeasureContext context) {
        var chartValues = context.getChartValuesProvider().getChartValues(null);
        return context.getHorizontalLayout() instanceof HorizontalLayout.FullWidth && addExtremeLabelPadding &&
                (chartValues.getMaxX() - chartValues.getMinX() - chartValues.getXStep() * offset) % (chartValues.getXStep() * spacing) == 0f;
    }

    @NonNull
    @Override
    public List<Float> getLabelValues(
        @NonNull ChartDrawContext context,
        @NonNull ClosedFloatingPointRange<Float> visibleXRange,
        @NonNull ClosedFloatingPointRange<Float> fullXRange
    ) {
        var chartValues = context.getChartValuesProvider().getChartValues(null);
        var remainder = ((visibleXRange.getStart() - chartValues.getMinX()) / chartValues.getXStep() - offset) % spacing;
        var firstValue = visibleXRange.getStart() + (spacing - remainder) % spacing * chartValues.getXStep();
        var minXOffset = chartValues.getMinX() % chartValues.getXStep();
        var values = Collections.<Float>emptyList();
        var multiplier = -LABEL_OVERFLOW_SIZE;
        while (true) {
            var potentialValue = firstValue + multiplier++ * spacing * chartValues.getXStep();
            potentialValue = chartValues.getXStep() * getRound((potentialValue - minXOffset) / chartValues.getXStep()) + minXOffset;
            if (potentialValue < chartValues.getMinX() || potentialValue == fullXRange.getStart()) continue;
            if (potentialValue > chartValues.getMaxX() || potentialValue == fullXRange.getEndInclusive()) break;
            values.add(potentialValue);
            if (potentialValue > visibleXRange.getEndInclusive()) break;
        }
        return values;
    }

    @Nullable
    @Override
    public List<Float> getLineValues(
        @NonNull ChartDrawContext context,
        @NonNull ClosedFloatingPointRange<Float> visibleXRange,
        @NonNull ClosedFloatingPointRange<Float> fullXRange
    ) {
        var chartValues = context.getChartValuesProvider().getChartValues(null);
        if (context.getHorizontalLayout() instanceof HorizontalLayout.FullWidth) return null;
        var remainder = (visibleXRange.getStart() - fullXRange.getStart()) % chartValues.getXStep();
        var firstValue = visibleXRange.getStart() + (chartValues.getXStep() - remainder) % chartValues.getXStep();
        var multiplier = -TICK_OVERFLOW_SIZE;
        var values = Collections.<Float>emptyList();
        while (true) {
            var potentialValue = firstValue + multiplier++ * chartValues.getXStep();
            if (potentialValue < fullXRange.getStart()) continue;
            if (potentialValue > fullXRange.getEndInclusive()) break;
            values.add(potentialValue);
            if (potentialValue > visibleXRange.getEndInclusive()) break;
        }
        return values;
    }

    @NonNull
    @Override
    public List<Float> getMeasuredLabelValues(@NonNull MeasureContext context, @NonNull HorizontalDimensions horizontalDimensions, @NonNull ClosedFloatingPointRange<Float> closedFloatingPointRange) {
        var chartValues = context.getChartValuesProvider().getChartValues(null);
        return List.of(chartValues.getMinX(), getHalf(chartValues.getMinX() + chartValues.getMaxX()), chartValues.getMaxX());
    }

    @Override
    public float getStartHorizontalAxisInset(@NonNull MeasureContext context, @NonNull HorizontalDimensions horizontalDimensions, float tickThickness) {
        var tickSpace = shiftExtremeTicks ? tickThickness : getHalf(tickThickness);
        if (context.getHorizontalLayout() instanceof HorizontalLayout.FullWidth) {
            var fullWidthTickSpace = tickSpace - horizontalDimensions.getUnscalableStartPadding();
            if (fullWidthTickSpace < 0) {
                fullWidthTickSpace = 0;
            }
            return fullWidthTickSpace;
        }
        return tickSpace;
    }

    @Override
    public float getEndHorizontalAxisInset(@NonNull MeasureContext context, @NonNull HorizontalDimensions horizontalDimensions, float tickThickness) {
        var tickSpace = shiftExtremeTicks ? tickThickness : getHalf(tickThickness);
        if (context.getHorizontalLayout() instanceof HorizontalLayout.FullWidth) {
            var fullWidthTickSpace = tickSpace - horizontalDimensions.getUnscalableEndPadding();
            if (fullWidthTickSpace < 0) {
                fullWidthTickSpace = 0;
            }
            return fullWidthTickSpace;
        }
        return tickSpace;
    }

    private static final int LABEL_OVERFLOW_SIZE = 2;
    private static final int TICK_OVERFLOW_SIZE = 1;

    @Override
    @Deprecated
    public float getMeasuredLabelClearance(@NonNull MeasureContext measureContext, @NonNull HorizontalDimensions horizontalDimensions, @NonNull ClosedFloatingPointRange<Float> closedFloatingPointRange) {
        return 0;
    }
}
