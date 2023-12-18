package io.github.kdetard.koki.feature.monitoring;

import static com.patrykandpatrick.vico.core.dimensions.MutableDimensionsKt.emptyDimensions;
import static com.patrykandpatrick.vico.core.extension.ColorExtensionsKt.copyColor;
import static com.patrykandpatrick.vico.views.dimensions.DimensionsExtensionsKt.dimensionsOf;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.NonNull;

import com.google.android.material.elevation.SurfaceColors;
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions;
import com.patrykandpatrick.vico.core.chart.insets.Insets;
import com.patrykandpatrick.vico.core.component.OverlayingComponent;
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent;
import com.patrykandpatrick.vico.core.component.shape.DashedShape;
import com.patrykandpatrick.vico.core.component.shape.LineComponent;
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent;
import com.patrykandpatrick.vico.core.component.shape.Shapes;
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner;
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape;
import com.patrykandpatrick.vico.core.component.text.TextComponent;
import com.patrykandpatrick.vico.core.context.MeasureContext;
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions;

import io.github.kdetard.koki.misc.ResourceUtils;

public class PillMarker {
    private static final int DEF_LAYOUT_SIZE = 100000;
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
    private static final int labelHorizontalPaddingValue = ResourceUtils.toDp(8);
    private static final int labelVerticalPaddingValue = ResourceUtils.toDp(4);
    private static final MutableDimensions labelPadding = dimensionsOf(labelHorizontalPaddingValue, labelVerticalPaddingValue);
    private static final int indicatorInnerAndCenterComponentPaddingValue = ResourceUtils.toDp(5);
    private static final int indicatorCenterAndOuterComponentPaddingValue = ResourceUtils.toDp(10);
    private static final int guidelineThickness = ResourceUtils.toDp(2);
    private static final DashedShape guidelineShape = new DashedShape(Shapes.INSTANCE.getPillShape(), GUIDELINE_DASH_LENGTH_DP, GUIDELINE_GAP_LENGTH_DP, DashedShape.FitStrategy.Resize);
    private static final int DEF_SHADOW_COLOR = 0x8A000000;

    private final Context context;
    private final MarkerComponent markerComponent;

    public MarkerComponent getMarkerComponent() {
        return markerComponent;
    }

    private Color getLabelBackgroundColor() {
        return Color.valueOf(SurfaceColors.SURFACE_0.getColor(context));
    }

    public PillMarker(Context context) {
        this.context = context;
        this.markerComponent = new MarkerComponent(
            getLabel(),
            getIndicator(),
            getGuideline()
        ) {
            @Override
            public void getInsets(@NonNull MeasureContext context, @NonNull Insets outInsets, @NonNull HorizontalDimensions horizontalDimensions) {
                outInsets.setTop(
                        getLabel().getHeight(context, null, DEF_LAYOUT_SIZE, DEF_LAYOUT_SIZE, 0f, true)
                                + context.getPixels(labelBackgroundShape.getTickSizeDp())
                                + context.getPixels(LABEL_BACKGROUND_SHADOW_RADIUS) * SHADOW_RADIUS_MULTIPLIER
                                - context.getPixels(LABEL_BACKGROUND_SHADOW_DY)
                );
            }
        };
        this.markerComponent.setIndicatorSizeDp(INDICATOR_SIZE_DP);
        this.markerComponent.setOnApplyEntryColor(entryColor -> {
            var outerComponentColor = Color.valueOf(entryColor);
            getIndicatorOuterComponent().setColor(copyColor(entryColor, INDICATOR_OUTER_COMPONENT_ALPHA, outerComponentColor.red(), outerComponentColor.green(), outerComponentColor.blue()));
            getIndicatorCenterComponent().setColor(entryColor);
            getIndicatorCenterComponent().setShadow(
                INDICATOR_CENTER_COMPONENT_SHADOW_RADIUS,
                0f,
                0f,
                entryColor,
                false
            );
            return null;
        });
    }

    private ShapeComponent shapeComponent;

    public ShapeComponent getLabelBackground() {
        if (shapeComponent != null) {
            return shapeComponent;
        }

        shapeComponent = new ShapeComponent(
            labelBackgroundShape,
            getLabelBackgroundColor().toArgb(),
            null,
            emptyDimensions(),
            0f,
            Color.TRANSPARENT
        );

        shapeComponent.setShadow(LABEL_BACKGROUND_SHADOW_RADIUS, LABEL_BACKGROUND_SHADOW_DY, 0f, DEF_SHADOW_COLOR, true);

        return shapeComponent;
    }

    private TextComponent textComponent;

    public TextComponent getLabel() {
        if (textComponent != null) {
            return textComponent;
        }

        var builder = new TextComponent.Builder();
        builder.setBackground(getLabelBackground());
        builder.setLineCount(LABEL_LINE_COUNT);
        builder.setPadding(labelPadding);
        builder.setTypeface(Typeface.MONOSPACE);

        textComponent = builder.build();

        return textComponent;
    }

    private ShapeComponent indicatorInnerComponent;

    public ShapeComponent getIndicatorInnerComponent() {
        if (indicatorInnerComponent != null) {
            return indicatorInnerComponent;
        }

        indicatorInnerComponent = new ShapeComponent(
            Shapes.INSTANCE.getPillShape(),
            getLabelBackgroundColor().toArgb(),
            null,
            emptyDimensions(),
            0f,
            Color.TRANSPARENT
        );

        return indicatorInnerComponent;
    }

    private ShapeComponent indicatorCenterComponent;

    public ShapeComponent getIndicatorCenterComponent() {
        if (indicatorCenterComponent != null) {
            return indicatorCenterComponent;
        }

        indicatorCenterComponent = new ShapeComponent(
            Shapes.INSTANCE.getPillShape(),
            Color.WHITE,
            null,
            emptyDimensions(),
            0f,
            Color.TRANSPARENT
        );

        indicatorCenterComponent.setShadow(INDICATOR_CENTER_COMPONENT_SHADOW_RADIUS, 0f, 0f, DEF_SHADOW_COLOR, true);

        return indicatorCenterComponent;
    }

    private ShapeComponent indicatorOuterComponent;

    public ShapeComponent getIndicatorOuterComponent() {
        if (indicatorOuterComponent != null) {
            return indicatorOuterComponent;
        }

        indicatorOuterComponent = new ShapeComponent(
            Shapes.INSTANCE.getPillShape(),
            Color.WHITE,
            null,
            emptyDimensions(),
            0f,
            Color.TRANSPARENT
        );

        return indicatorOuterComponent;
    }

    private OverlayingComponent indicator;

    public OverlayingComponent getIndicator() {
        if (indicator != null) {
            return indicator;
        }

        indicator = new OverlayingComponent(
            getIndicatorOuterComponent(),
            new OverlayingComponent(
                getIndicatorCenterComponent(),
                getIndicatorInnerComponent(),
                indicatorInnerAndCenterComponentPaddingValue
            ),
            indicatorCenterAndOuterComponentPaddingValue
        );

        return indicator;
    }

    private LineComponent guideline;

    public LineComponent getGuideline() {
        if (guideline != null) {
            return guideline;
        }

        var lineColor = getLabelBackgroundColor();

        guideline = new LineComponent(
            Color.argb(GUIDELINE_ALPHA, lineColor.red(), lineColor.green(), lineColor.blue()),
            guidelineThickness,
            guidelineShape,
            null,
            emptyDimensions(),
            0f,
            Color.TRANSPARENT
        );

        return guideline;
    }
}



