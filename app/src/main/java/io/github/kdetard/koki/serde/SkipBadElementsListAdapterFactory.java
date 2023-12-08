package io.github.kdetard.koki.serde;

import androidx.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public class SkipBadElementsListAdapterFactory implements JsonAdapter.Factory {
    @Override
    public JsonAdapter<?> create(@NonNull Type type, Set<? extends Annotation> annotations, @NonNull Moshi moshi) {
        if (!annotations.isEmpty() || Types.getRawType(type) != List.class) {
            return null;
        }
        Type elementType = Types.collectionElementType(type, List.class);
        JsonAdapter<Object> elementAdapter = moshi.adapter(elementType);
        return new SkipBadElementsListAdapter(elementAdapter);
    }
}
