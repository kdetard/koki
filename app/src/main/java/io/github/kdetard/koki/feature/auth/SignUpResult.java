package io.github.kdetard.koki.feature.auth;

import android.content.Context;

import androidx.annotation.NonNull;

import dagger.hilt.internal.aggregatedroot.codegen._io_github_kdetard_koki_App;
import io.github.kdetard.koki.R;

public enum SignUpResult {
    EMPTY(R.string.signup_empty_response),
    INVALID(R.string.signup_invalid_fields),
    SPECIFY(R.string.signup_empty_fields),
    TIMEOUT(R.string.signup_timeout),
    EMAIL_EXISTS(R.string.signup_email_exists),
    USERNAME_EXISTS(R.string.signup_username_exists),
    NULL(R.string.signup_null),
    UNKNOWN(R.string.signup_unknown),
    SUCCESS(R.string.signup_success),
    ;

    private final int resId;

    SignUpResult(final int resId) {
        this.resId = resId;
    }

    @NonNull
    public String getText(Context context) {
        return context.getString(resId);
    }
}
