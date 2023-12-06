package io.github.kdetard.koki.utils;

public class FormResult<T> {
    private final T inputLayout;
    private final String text;
    private final String error;
    private final boolean strict;

    public FormResult(String text, String error) {
        this(null, text, error, true);
    }

    public FormResult(String text, String error, boolean strict) {
        this(null, text, error, strict);
    }

    public FormResult(T inputLayout, String text, String error) {
        this(inputLayout, text, error, true);
    }

    public FormResult(T inputLayout, String text, String error, boolean strict) {
        this.text = text;
        this.inputLayout = inputLayout;
        this.error = error;
        this.strict = strict;
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

    public boolean isStrict() { return strict; }
}
