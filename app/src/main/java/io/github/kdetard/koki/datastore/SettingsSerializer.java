package io.github.kdetard.koki.datastore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.datastore.core.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.kdetard.koki.Settings;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import timber.log.Timber;

public class SettingsSerializer implements Serializer<Settings> {
    @Override
    public Settings getDefaultValue() {
        return Settings.getDefaultInstance();
    }

    @Nullable
    @Override
    public Object readFrom(@NonNull InputStream input, @NonNull Continuation<? super Settings> continuation) {
        try {
            return Settings.parseFrom(input);
        } catch (IOException exception) {
            Timber.d(exception, "Cannot read proto");
            return null;
        }
    }

    @Nullable
    @Override
    public Object writeTo(Settings t, @NonNull OutputStream output, @NonNull Continuation<? super Unit> continuation) {
        try {
            t.writeTo(output);
            return t;
        } catch (IOException e) {
            return null;
        }
    }
}
