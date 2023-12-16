package io.github.kdetard.koki.openremote.models;

public record JsonPath(
    String path,
    Boolean returnFirst,
    Boolean returnLast
) implements OpenRemoteObject {
    @Override
    public String type() { return TYPE; }

    public static final String TYPE = "jsonPath";
}
