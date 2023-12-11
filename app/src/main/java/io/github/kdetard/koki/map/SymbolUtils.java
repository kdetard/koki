package io.github.kdetard.koki.map;

import static java.util.Map.entry;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.Map;
import java.util.Objects;

import io.github.kdetard.koki.R;

// safe way to get the symbol for a given asset
public class SymbolUtils {
    public static final String DEFAULT_SYMBOL = "default";
    public static final String WEATHER_SYMBOL = "weather";
    public static final String LIGHT_SYMBOL = "light-symbol";
    public static final String GROUP_SYMBOL = "group-symbol";
    public static final String HTTP_SYMBOL = "http-symbol";
    public static final String MQTT_SYMBOL = "mqtt-symbol";

    public static final Map<String, Integer> SYMBOLS = Map.ofEntries(
        entry(DEFAULT_SYMBOL, R.drawable.or_symbol_selector_24dp),
        entry(WEATHER_SYMBOL, R.drawable.or_weather_symbol_selector_24dp),
        entry(LIGHT_SYMBOL, R.drawable.or_light_symbol_selector_24dp),
        entry(GROUP_SYMBOL, R.drawable.or_group_symbol_selector_24dp),
        entry(HTTP_SYMBOL, R.drawable.or_http_symbol_selector_24dp),
        entry(MQTT_SYMBOL, R.drawable.or_mqtt_symbol_selector_24dp)
    );

    public static final Map<String, Integer> ICONS = Map.ofEntries(
        entry(DEFAULT_SYMBOL, R.drawable.ic_assets_outline_24dp),
        entry(WEATHER_SYMBOL, R.drawable.ic_partly_cloudy_day_outline_24dp),
        entry(LIGHT_SYMBOL, R.drawable.ic_lightbulb_outline_24dp),
        entry(GROUP_SYMBOL, R.drawable.ic_network_node_24dp),
        entry(HTTP_SYMBOL, R.drawable.ic_http_24dp),
        entry(MQTT_SYMBOL, R.drawable.ic_rss_feed_24dp)
    );

    public static Style.Builder toBuilder(Context context) {
        var builder = new Style.Builder();
        for (Map.Entry<String, Integer> entry : SYMBOLS.entrySet()) {
            builder.withImage(entry.getKey(),
                    Objects.requireNonNull(ResourcesCompat.getDrawable(context.getResources(), entry.getValue(), null)));
        }
        return builder;
    }

    public static SymbolOptions defaultSymbolOptions() {
        return new SymbolOptions()
                .withIconSize(10f)
                .withIconImage(DEFAULT_SYMBOL)
                .withIconSize(0.5f);
    }
}
