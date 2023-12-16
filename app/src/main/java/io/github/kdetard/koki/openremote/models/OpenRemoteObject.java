package io.github.kdetard.koki.openremote.models;

public sealed interface OpenRemoteObject permits Asset, Attribute, HTTPAgentAsset.HttpAgentLink, JsonPath, LightAsset.LightAgentLink, Point, Widget {
     String type();
}
