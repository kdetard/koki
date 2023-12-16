package io.github.kdetard.koki.form;

public record SignInFormResult<T>(FormResult<T> userName, FormResult<T> password) {
}
