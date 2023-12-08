package io.github.kdetard.koki.openremote.models;

import static java.util.Map.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenRemoteModel {
    public static final Map<String, Class<? extends OpenRemoteObject>> ObjectMap = Map.ofEntries(
        entry(Point.TYPE, Point.class),
        entry(HTTPAgentAsset.HttpAgentLink.TYPE, HTTPAgentAsset.HttpAgentLink.class)
    );

    public static final List<Class<? extends OpenRemoteObject>> ObjectClasses = new ArrayList<>(ObjectMap.values());

    public static final List<String> ObjectSubTypes = new ArrayList<>(ObjectMap.keySet());

    public static final Map<String, Class<? extends Asset<? extends AssetAttribute>>> AssetMap = Map.ofEntries(
        entry(ConsoleAsset.TYPE, ConsoleAsset.class),
        entry(GroupAsset.TYPE, GroupAsset.class),
        entry(HTTPAgentAsset.TYPE, HTTPAgentAsset.class),
        entry(LightAsset.TYPE, LightAsset.class),
        entry(MQTTAgentAsset.TYPE, MQTTAgentAsset.class),
        entry(WeatherAsset.TYPE, WeatherAsset.class)
    );

    public static final List<Class<? extends Asset<? extends AssetAttribute>>> AssetClasses = new ArrayList<>(AssetMap.values());

    public static final List<String> AssetSubTypes = new ArrayList<>(AssetMap.keySet());

    public static final Map<String, Class<? extends Attribute>> AttributeMap = Map.ofEntries(
        // attributes
        entry(BooleanAttribute.TYPE, BooleanAttribute.class),
        entry(ChildAssetType.TYPE, ChildAssetType.class),
        entry(ColourRGB.TYPE, ColourRGB.class),
        entry(ConnectionStatus.TYPE, ConnectionStatus.class),
        entry(Direction.TYPE, Direction.class),
        entry(Email.TYPE, Email.class),
        entry(GeoJsonPoint.TYPE, GeoJsonPoint.class),
        entry(HostAddress.TYPE, HostAddress.class),
        entry(HttpUrl.TYPE, HttpUrl.class),
        entry(Json.TYPE, Json.class),
        entry(JsonObject.TYPE, JsonObject.class),
        entry(MultivaluedTextMap.TYPE, MultivaluedTextMap.class),
        entry(Number.TYPE, Number.class),
        entry(PortNumber.TYPE, PortNumber.class),
        entry(PositiveInteger.TYPE, PositiveInteger.class),
        entry(PositiveNumber.TYPE, PositiveNumber.class),
        entry(Text.TYPE, Text.class),
        entry(TextList.TYPE, TextList.class)
    );

    public static final List<Class<? extends Attribute>> AttributeClasses = new ArrayList<>(AttributeMap.values());

    public static final List<String> AttributeSubTypes = new ArrayList<>(AttributeMap.keySet());

    public static final Map<String, Class<? extends Widget>> WidgetMap = Map.ofEntries(
        entry(LineChartWidget.TYPE, LineChartWidget.class),
        entry(MapWidget.TYPE, MapWidget.class)
    );

    public static final List<Class<? extends Widget>> WidgetClasses = new ArrayList<>(WidgetMap.values());

    public static final List<String> WidgetSubTypes = new ArrayList<>(WidgetMap.keySet());
}
