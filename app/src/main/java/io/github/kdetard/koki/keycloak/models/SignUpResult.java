package io.github.kdetard.koki.keycloak.models;

import androidx.annotation.NonNull;

public enum SignUpResult {
    EMPTY("Empty response"),
    INVALID("Invalid fields"),
    SPECIFY("Empty fields"),
    TIMEOUT("Timeout"),
    EMAIL_EXISTS("Email already exists"),
    USERNAME_EXISTS("Username already exists"),
    NULL("Null response"),
    UNKNOWN("Unknown error occurred"),
    SUCCESS("Success"),
    ;

    private final String text;

    SignUpResult(final String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
