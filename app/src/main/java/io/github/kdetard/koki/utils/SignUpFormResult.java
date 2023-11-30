package io.github.kdetard.koki.utils;

public class SignUpFormResult<T> {
    private final FormResult<T> username;
    private final FormResult<T> email;
    private final FormResult<T> password;

    public SignUpFormResult(FormResult<T> username, FormResult<T> email, FormResult<T> password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public FormResult<T> getUsername() {
        return username;
    }

    public FormResult<T> getEmail() { return email; }

    public FormResult<T> getPassword() {
        return password;
    }
}
