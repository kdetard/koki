package io.github.kdetard.koki.datastore;

/*public class DataStoreValue<T> {

    private final DataStore<Preferences> dataStore;
    private final Preferences.Key<T> key;
    private final java.util.function.Function<Any?, T> reader;
    private final java.util.function.Function<T, Any?> writer;

    public DataStoreValue(DataStore<Preferences> dataStore, Preferences.Key<T> key, java.util.function.Function<Any?, T> reader, java.util.function.Function<T, Any?> writer) {
        this.dataStore = dataStore;
        this.key = key;
        this.reader = reader;
        this.writer = writer;
    }

    public String getKeyName() {
        return key.getName();
    }

    public Flow<T> getFlow() {
        return dataStore.data
                .map(prefs -> reader.apply(prefs.get(key)));
    }

    public static class Updated<T> {
        private final T old;
        private final T new;

        public Updated(T old, T new) {
            this.old = old;
            this.new = new;
        }

        public T getOld() {
            return old;
        }

        public T getNew() {
            return new;
        }
    }

    public Updated<T> update(java.util.function.Function<T, T> update) throws ExecutionException, InterruptedException {
        T[] values = new T[2];

        dataStore.updateData(prefs -> {
            T before = reader.apply(prefs.get(key));
            values[0] = before;

            T after = update.apply(before);
            values[1] = after != null ? writer.apply(after) : null;

            return prefs.toMutablePreferences()
                    .set(key, after)
                    .toPreferences();
        });

        return new Updated<>(values[0], values[1]);
    }
}

public class DataStoreExtensions<T> {
    public static <T extends Object> DataStoreValue<T> createValue(DataStore<Preferences> dataStore, Preferences.Key<T> key, java.util.function.Function<Any?, T> reader, java.util.function.Function<T, Any?> writer) {
        return new DataStoreValue<>(dataStore, key, reader, writer);
    }

    public static <T> Preferences.Key<T> basicKey(String key, T defaultValue) {
        if (defaultValue instanceof Boolean) {
            return booleanPreferencesKey(key);
        } else if (defaultValue instanceof String) {
            return stringPreferencesKey(key);
        } else if (defaultValue instanceof Integer) {
            return intPreferencesKey(key);
        } else if (defaultValue instanceof Long) {
            return longPreferencesKey(key);
        } else if (defaultValue instanceof Float) {
            return floatPreferencesKey(key);
        } else {
            throw new NotImplementedError();
        }
    }

    public static <T> java.util.function.Function<Any?, T> basicReader(T defaultValue) {
        return rawValue -> {
            T value = rawValue != null ? (T) rawValue : defaultValue;
            return value;
        };
    }

    public static <T> java.util.function.Function<T, Any?> basicWriter() {
        return value -> {
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                return (String) value;
            } else if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Float) {
                return (Float) value;
            } else {
                return null;
            }
        };
    }

    public static <T extends Object> DataStoreValue<T> createValue(DataStore<Preferences> dataStore, String key, T defaultValue) {
        return createValue(dataStore, basicKey(key, defaultValue), basicReader(defaultValue), basicWriter());
    }
}*/
