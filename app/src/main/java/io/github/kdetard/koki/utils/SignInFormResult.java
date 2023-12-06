package io.github.kdetard.koki.utils;

public class SignInFormResult<T> {
    private final FormResult<T> userName;
    private final FormResult<T> password;

    public SignInFormResult(FormResult<T> userName, FormResult<T> password) {
        this.userName = userName;
        this.password = password;
    }

    public FormResult<T> getUserName() {
        return userName;
    }

    public FormResult<T> getPassword() {
        return password;
    }
}
