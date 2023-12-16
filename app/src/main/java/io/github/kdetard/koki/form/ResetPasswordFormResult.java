package io.github.kdetard.koki.form;

public record ResetPasswordFormResult<T>(FormResult<T> userNameOrEmail) {
}
