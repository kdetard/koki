package io.github.kdetard.koki.serde;

import androidx.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SkipBadElementsListAdapter extends JsonAdapter<List<Object>> {
    private final JsonAdapter<Object> elementAdapter;

    public SkipBadElementsListAdapter(JsonAdapter<Object> elementAdapter) {
        this.elementAdapter = elementAdapter;
    }

    @Override
    public List<Object> fromJson(JsonReader reader) throws IOException {
        List<Object> result = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            try {
                JsonReader peeked = reader.peekJson();
                result.add(elementAdapter.fromJson(peeked));
            } catch (JsonDataException ignored) {
            }
            reader.skipValue();
        }
        reader.endArray();
        return result;
    }

    @Override
    public void toJson(@NonNull JsonWriter writer, List<Object> value) throws IOException {
        if (value == null) {
            throw new NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.");
        }
        writer.beginArray();
        for (final var obj : value) {
            elementAdapter.toJson(writer, obj);
        }
        writer.endArray();
    }
}

