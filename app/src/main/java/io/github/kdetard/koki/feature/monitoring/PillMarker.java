package io.github.kdetard.koki.feature.monitoring;

import static com.patrykandpatrick.vico.views.dimensions.DimensionsExtensionsKt.dimensionsOf;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;

import androidx.annotation.NonNull;

import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.resources.MaterialResources;
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions;
import com.patrykandpatrick.vico.core.chart.insets.HorizontalInsets;
import com.patrykandpatrick.vico.core.chart.insets.Insets;
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider;
import com.patrykandpatrick.vico.core.component.shape.DashedShape;
import com.patrykandpatrick.vico.core.component.shape.Shapes;
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner;
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape;
import com.patrykandpatrick.vico.core.component.text.TextComponent;
import com.patrykandpatrick.vico.core.context.DrawContext;
import com.patrykandpatrick.vico.core.context.MeasureContext;
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions;
import com.patrykandpatrick.vico.core.marker.Marker;

import java.util.List;

import io.github.kdetard.koki.misc.ResourceUtils;

public class PillMarker implements Marker {
        private static final float DEF_MARKER_TICK_SIZE = 6f;
        private static final float LABEL_BACKGROUND_SHADOW_RADIUS = 4f;
        private static final float LABEL_BACKGROUND_SHADOW_DY = 2f;
        private static final int LABEL_LINE_COUNT = 1;
        private static final float GUIDELINE_ALPHA = .2f;
        private static final float INDICATOR_SIZE_DP = 36f;
        private static final int INDICATOR_OUTER_COMPONENT_ALPHA = 32;
        private static final float INDICATOR_CENTER_COMPONENT_SHADOW_RADIUS = 12f;
        private static final float GUIDELINE_DASH_LENGTH_DP = 8f;
        private static final float GUIDELINE_GAP_LENGTH_DP = 4f;
        private static final float SHADOW_RADIUS_MULTIPLIER = 1.3f;

        private static final MarkerCorneredShape labelBackgroundShape = new MarkerCorneredShape(Corner.Companion.getFullyRounded(), DEF_MARKER_TICK_SIZE);
        ;
        private static final int labelHorizontalPaddingValue = ResourceUtils.toDp(8);
        private static final int labelVerticalPaddingValue = ResourceUtils.toDp(4);
        private static final MutableDimensions labelPadding = dimensionsOf(labelHorizontalPaddingValue, labelVerticalPaddingValue);
        private static final int indicatorInnerAndCenterComponentPaddingValue = ResourceUtils.toDp(5);
        private static final int indicatorCenterAndOuterComponentPaddingValue = ResourceUtils.toDp(10);
        private static final int guidelineThickness = ResourceUtils.toDp(2);
        private static final DashedShape guidelineShape = new DashedShape(Shapes.INSTANCE.getPillShape(), GUIDELINE_DASH_LENGTH_DP, GUIDELINE_GAP_LENGTH_DP, DashedShape.FitStrategy.Resize);

        @Override
        public void getHorizontalInsets(@NonNull MeasureContext measureContext, float v, @NonNull HorizontalInsets horizontalInsets) {

        }

        @Override
        public void getInsets(@NonNull MeasureContext measureContext, @NonNull Insets insets, @NonNull HorizontalDimensions horizontalDimensions) {

        }

        @Override
        public void draw(@NonNull DrawContext drawContext, @NonNull RectF rectF, @NonNull List<EntryModel> list, @NonNull ChartValuesProvider chartValuesProvider) {

        }

        @Override
        public void draw(@NonNull DrawContext drawContext, @NonNull RectF rectF, @NonNull List<EntryModel> list) {

        }

        /*Color labelBackgroundColor(Context context) {
                return SurfaceColors.SURFACE_0.getColor(context);
        } com.google.android.material.R.color.surface MaterialTheme.colorScheme.surface;
        val labelBackground =
                remember(labelBackgroundColor) {
                        ShapeComponent(labelBackgroundShape, labelBackgroundColor.toArgb()).setShadow(
                                radius = LABEL_BACKGROUND_SHADOW_RADIUS,
                                dy = LABEL_BACKGROUND_SHADOW_DY,
                                applyElevationOverlay = true,
                        )
                }
        TextComponent label() {
                var textComponent = new TextComponent() {};
                textComponent.setBackground(labelBackground);
                textComponent.setLineCount(LABEL_LINE_COUNT);
                textComponent.setPadding(labelPadding);
                textComponent.setTypeface(Typeface.MONOSPACE);
                return textComponent;
        }
        val indicatorInnerComponent = rememberShapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.surface)
        val indicatorCenterComponent = rememberShapeComponent(Shapes.pillShape, Color.White)
        val indicatorOuterComponent = rememberShapeComponent(Shapes.pillShape, Color.White)
        val indicator =
                overlayingComponent(
                        outer = indicatorOuterComponent,
                        inner =
                                overlayingComponent(
                                        outer = indicatorCenterComponent,
                                        inner = indicatorInnerComponent,
                                        innerPaddingAll = indicatorInnerAndCenterComponentPaddingValue,
                                ),
                        innerPaddingAll = indicatorCenterAndOuterComponentPaddingValue,
                )
        val guideline =
        rememberLineComponent(
        MaterialTheme.colorScheme.onSurface.copy(GUIDELINE_ALPHA),
        guidelineThickness,
        guidelineShape,
        )
        return remember(label, indicator, guideline) {
        object : MarkerComponent(label, indicator, guideline) {
        init {
        indicatorSizeDp = INDICATOR_SIZE_DP
        onApplyEntryColor = { entryColor ->
        indicatorOuterComponent.color = entryColor.copyColor(INDICATOR_OUTER_COMPONENT_ALPHA)
        with(indicatorCenterComponent) {
        color = entryColor
        setShadow(radius = INDICATOR_CENTER_COMPONENT_SHADOW_RADIUS, color = entryColor)
        }
        }
        }

        override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
        ) = with(context) {
        outInsets.top = label.getHeight(context) + labelBackgroundShape.tickSizeDp.pixels +
        LABEL_BACKGROUND_SHADOW_RADIUS.pixels * SHADOW_RADIUS_MULTIPLIER -
        LABEL_BACKGROUND_SHADOW_DY.pixels
        }
        }
        }
        }*/
}



