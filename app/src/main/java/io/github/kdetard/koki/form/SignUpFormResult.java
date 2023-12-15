package io.github.kdetard.koki.form;

public record SignUpFormResult<T>(FormResult<T> username, FormResult<T> email,
                                  FormResult<T> password) {
}
