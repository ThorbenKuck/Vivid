package com.vivid.clients.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;

import java.time.Instant;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Collections.emptyMap;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"id", "name", "key", "timestamp", "enabled", "flags", "metadata"})
public record Feature(
        String id,
        String name,
        String key,
        Boolean enabled,
        Map<String, Boolean> flags,
        Map<String, MetadataValue<?>> metadata,
        Instant timestamp
) {

    private static final Feature EMPTY = new Feature(
                    "",
                    "",
                    "",
                    false,
                    emptyMap(),
                    emptyMap(),
                    Instant.ofEpochMilli(0)
            );

    public static Feature empty() {
        return EMPTY;
    }

    /**
     * Get a metadata value for the feature, checking its type.
     *
     * @param name the name of the metadata value
     * @param type the expected type of the metadata value
     * @return the metadata value if it exists and has the correct type, or null if it does not exist
     * @throws ClassCastException if the metadata value exists but has a different type
     */
    @Nullable
    public <S, T extends MetadataValue<S>> T checkedMetadataValue(String name, Class<T> type) {
        MetadataValue<?> unchecked = metadata.get(name);
        if (unchecked == null) {
            return null;
        }
        if (type.isInstance(unchecked)) {
            return (T) unchecked;
        } else {
            throw new ClassCastException("Expected metadata " + name +" to be of type " + type.getName() + ", but was of type " + unchecked.getClass().getName());
        }
    }

    @Nullable
    public Feature nullIfEmpty() {
        if (this.isEmpty()) {
            return null;
        } else {
            return this;
        }
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Feature feature = (Feature) o;
        return Objects.equals(id, feature.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Feature(id='" + id + "', name='" + name + "')";
    }

    public static Feature newInstance(Consumer<Builder> consumer) {
        Builder builder = builder();
        consumer.accept(builder);
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Feature feature) {
        return new Builder(feature);
    }

    public static class Builder {
        @Nullable private String id = null;
        @Nullable private String name = null;
        @Nullable private String key = null;
        private boolean enabled = true;
        private final Map<String, Boolean> flags  = new HashMap<>();
        private final Map<String, MetadataValue<?>> metadata = new HashMap<>();
        private Instant timestamp = Instant.now();

        public Builder() {}

        public Builder(Feature feature) {
            this.id = feature.id;
            this.name = feature.name;
            this.key = feature.key;
            this.enabled = feature.enabled;
            this.flags.putAll(feature.flags);
            this.metadata.putAll(feature.metadata);
            this.timestamp = feature.timestamp;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder flags(Map<String, Boolean> flags) {
            this.flags.putAll(flags);
            return this;
        }

        public Builder flag(String key, Boolean value) {
            this.flags.put(key, value);
            return this;
        }

        public Builder metadata(Map<String, MetadataValue<?>> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public <S, T extends MetadataValue<S>> Builder metadata(String key, T metadata) {
            this.metadata.put(key, metadata);
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Feature build() {
            Objects.requireNonNull(id, "id must not be null");
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(key, "key must not be null");
            return new Feature(id, name, key, enabled, flags, metadata, timestamp);
        }
    }
}
