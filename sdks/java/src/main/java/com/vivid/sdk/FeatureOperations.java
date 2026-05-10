package com.vivid.sdk;

import com.vivid.clients.api.Feature;
import com.vivid.clients.api.MetadataValue;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public interface FeatureOperations {

    FeatureOperations UNKNOWN = new Unknown();

    static FeatureOperations of(@Nullable Feature feature) {
        if (feature == null) {
            return UNKNOWN;
        }
        return new Api(feature);
    }

    /**
     * Check if the main feature flag is enabled.
     *
     * @return true if enabled, false if disabled, or null if the feature state is unknown
     */
    @Nullable
    Boolean isEnabled();

    /**
     * Check if the main feature flag is enabled, with a default value.
     *
     * @param defaultValue the default value to return if the feature state is unknown
     * @return true if enabled, false if disabled, or the default value if the state is unknown
     */
    default boolean isEnabled(boolean defaultValue) {
        return Objects.requireNonNullElse(isEnabled(), defaultValue);
    }

    /**
     * Check if the main feature flag is enabled, with a default value.
     *
     * @param defaultValue the default value to return if the feature state is unknown
     * @return true if enabled, false if disabled, or the default value if the state is unknown
     */
    default boolean isEnabled(BooleanSupplier defaultValue) {
        return Objects.requireNonNullElseGet(isEnabled(), defaultValue::getAsBoolean);
    }

    /**
     * Check if a sub-flag of the feature is enabled.
     *
     * @param name the name of the sub-flag
     * @return true if enabled, false if disabled, or null if the sub-flag state is unknown
     */
    @Nullable
    Boolean isEnabled(String name);

    /**
     * Check if a sub-flag of the feature is enabled, with a default value.
     *
     * @param name         the name of the sub-flag
     * @param defaultValue the default value to return if the sub-flag state is unknown
     * @return true if enabled, false if disabled, or the default value if the state is unknown
     */
    default boolean isEnabled(String name, Boolean defaultValue) {
        return Objects.requireNonNullElse(isEnabled(name), defaultValue);
    }

    /**
     * Check if a sub-flag of the feature is enabled, with a default value.
     *
     * @param name            the name of the sub-flag
     * @param defaultProvider the provider to get the default value if the sub-flag state is unknown
     * @return true if enabled, false if disabled, or the default value if the state is unknown
     */
    default boolean isEnabled(String name, BooleanSupplier defaultProvider) {
        return Objects.requireNonNullElseGet(isEnabled(name), defaultProvider::getAsBoolean);
    }

    default void ifEnabled(Runnable action) {
        ifEnabled(false, action);
    }

    default void ifEnabled(boolean defaultValue, Runnable action) {
        if (isEnabled(defaultValue)) {
            action.run();
        }
    }

    default void ifEnabled(String flag, Runnable action) {
        ifEnabled(flag, false, action);
    }

    default void ifEnabled(String flag, boolean defaultValue, Runnable action) {
        if (isEnabled(flag, defaultValue)) {
            action.run();
        }
    }

    @Nullable
    default MetadataValue<?> getMetadata(String name) {
        return getMetadata(name, MetadataValue.class);
    }

    /**
     * Get a metadata value for the feature.
     *
     * @param name the name of the metadata value
     * @param type the type of the metadata value
     * @return the [MetadataValue] instance, or null if it does not exist or has a different type
     */
    @Nullable
    <S, T extends MetadataValue<S>> T getMetadata(String name, Class<T> type);

    /**
     * Get a metadata value for the feature, with a default value.
     *
     * @param name         the name of the metadata value
     * @param type         the type of the metadata value
     * @param defaultValue a function providing the default value
     * @return the [MetadataValue] instance, or the default value if it does not exist or has a different type
     */
    default <S, T extends MetadataValue<S>> T getMetadata(String name, Class<T> type, Supplier<T> defaultValue) {
        return Objects.requireNonNullElseGet(getMetadata(name, type), defaultValue);
    }

    /**
     * Implementation of [FeatureOperations] that uses a [Feature] state.
     */
    class Api implements FeatureOperations {

        private final Feature feature;

        public Api(Feature feature) {
            this.feature = feature;
        }

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        public Boolean isEnabled() {
            return feature.enabled();
        }

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        public Boolean isEnabled(String name) {
            Boolean flagValue = feature.flags().get(name);
            if (flagValue == null) return null;
            return feature.enabled() && flagValue;

        }

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        public <S, T extends MetadataValue<S>> T getMetadata(String name, Class<T> type) {
            MetadataValue<?> unchecked = feature.metadata().get(name);
            if (unchecked == null) return null;

            if (type.isInstance(unchecked)) {
                return (T) unchecked;
            } else {
                throw new ClassCastException("Expected metadata " + name + " to be of type " + type.getName() + ", but was of type " + unchecked.getClass().getName());
            }

        }
    }

    /**
     * Implementation of [FeatureOperations] for unknown features.
     * <p>
     * This implementation always returns null or the default value.
     */
    class Unknown implements FeatureOperations {

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        public Boolean isEnabled() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        public Boolean isEnabled(String name) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        public <S, T extends MetadataValue<S>> T getMetadata(String name, Class<T> type) {
            return null;
        }
    }
}
