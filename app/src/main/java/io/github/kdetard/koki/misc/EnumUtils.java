package io.github.kdetard.koki.misc;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnumUtils {
    public static<K, V> Map<V, K> toEnumMap(final K[] ks, Function<K, V> transform) {
        return Arrays.stream(ks)
            .collect(Collectors.toMap(transform, k -> k));
    }
}
