package io.github.kdetard.koki.utils;

public class FormResult<T> {
    private final T inputLayout;
    private final String text;
    private final String error;

    public FormResult(String text, String error) {
        this(null, text, error);
    }

    public FormResult(T inputLayout, String text, String error) {
        this.text = text;
        this.inputLayout = inputLayout;
        this.error = error;
    }

    public T getInputLayout() {
        return inputLayout;
    }

    public String getText() {
        return text;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return error.isEmpty();
    }
}
