package io.github.kdetard.koki.utils;

import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import kotlin.jvm.functions.Function2;

public class FormUtils {
    public static final String EmailRegex = "^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,}$";

    public static Observable<FormResult<TextInputLayout>> textChanges(
            TextInputLayout inputLayout,
            Function2<String, TextInputLayout, FormResult<TextInputLayout>> predicate
    ) {
        return textChanges(inputLayout, Objects.requireNonNull(inputLayout.getEditText()), predicate);
    }

    public static Observable<FormResult<String>> textChanges(
            EditText editText,
            Function<String, FormResult<String>> predicate
    ) {
        return RxTextView
                .textChanges(editText)
                .skip(1)
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString)
                .map(predicate::apply)
                .distinctUntilChanged();
    }

    public static Observable<FormResult<TextInputLayout>> textChanges(
            TextInputLayout inputLayout,
            EditText editText,
            Function2<String, TextInputLayout, FormResult<TextInputLayout>> predicate
    ) {
        return RxTextView
                .textChanges(editText)
                .skip(1)
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString)
                .map(str -> predicate.invoke(str, inputLayout))
                .distinctUntilChanged();
    }

    @NonNull
    public static String handleInput(EditText editText, Function<String, String> callback) {
        var inputStr = editText.getText().toString();
        var callbackStr = callback.apply(inputStr);
        if (callbackStr == null) return inputStr;
        editText.setError(callbackStr);
        return "";
    }

    public static FormResult<TextInputLayout> isValidUsernameOrEmail(String usernameOrEmail, TextInputLayout inputLayout) {
        final var stringFormResult = isValidUsernameOrEmail(usernameOrEmail);
        return new FormResult<>(inputLayout, usernameOrEmail, stringFormResult.getError());
    }

    public static FormResult<String> isValidUsernameOrEmail(String usernameOrEmail) {
        var error = isValidUsername(usernameOrEmail).getError();
        if (usernameOrEmail.contains("@")) {
            error = isValidEmail(usernameOrEmail).getError();
        }
        return new FormResult<>(usernameOrEmail, error);
    }

    public static FormResult<TextInputLayout> isValidUsername(String username, TextInputLayout inputLayout) {
        final var stringFormResult = isValidUsername(username);
        return new FormResult<>(inputLayout, username, stringFormResult.getError());
    }

    public static FormResult<String> isValidUsername(String username) {
        var error = "";
        if (username.isEmpty()) {
            error = "Username is empty";
        }
        return new FormResult<>(username, error);
    }

    public static FormResult<TextInputLayout> isValidEmail(String email, TextInputLayout inputLayout) {
        final var stringFormResult = isValidEmail(email);
        return new FormResult<>(inputLayout, email, stringFormResult.getError());
    }

    public static FormResult<String> isValidEmail(String email) {
        var error = "";
        if (!email.matches(EmailRegex)) {
            error = "Invalid email";
        }
        return new FormResult<>(email, error);
    }

    public static FormResult<TextInputLayout> isValidPassword(String password, TextInputLayout inputLayout) {
        final var stringFormResult = isValidPassword(password);
        return new FormResult<>(inputLayout, password, stringFormResult.getError());
    }

    public static FormResult<String> isValidPassword(String password) {
        var error = "";
        if (password.length() < 8) {
            error = "Password must be over 8 characters";
        }
        return new FormResult<>(password, error, false);
    }
}
