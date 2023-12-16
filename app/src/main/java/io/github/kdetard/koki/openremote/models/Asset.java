package io.github.kdetard.koki.openremote.models;

import java.util.List;

public sealed interface Asset<T> extends OpenRemoteObject permits ConsoleAsset, GroupAsset, HTTPAgentAsset, LightAsset, MQTTAgentAsset, WeatherAsset {
    String id();
    int version();
    long createdOn();
    String name();
    boolean accessPublicRead();
    String realm();
    List<String> path();
    T attributes();
    @Override
    String type();
}
