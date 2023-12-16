package io.github.kdetard.koki.keycloak.models;

import androidx.annotation.NonNull;

public enum ResetPasswordResult {
    EMPTY("Empty response"),
    FAILED_TO_SEND_MAIL("Failed to send mail"),
    SPECIFY("Empty fields"),
    TIMEOUT("Timeout"),
    NULL("Null response"),
    UNKNOWN("Unknown error occurred"),
    SUCCESS("Success"),
    ;

    private final String text;

    ResetPasswordResult(final String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
