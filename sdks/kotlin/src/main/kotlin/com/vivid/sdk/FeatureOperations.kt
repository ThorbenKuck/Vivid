package com.vivid.sdk

import com.vivid.sdk.api.Feature
import com.vivid.sdk.api.MetadataValue
import kotlin.reflect.KClass

/**
 * Interface for checking feature flag states and metadata.
 */
interface FeatureOperations {

    /**
     * Check if the main feature flag is enabled.
     *
     * @return true if enabled, false if disabled, or null if the feature state is unknown
     */
    fun isEnabled(): Boolean?

    /**
     * Check if the main feature flag is enabled, with a default value.
     *
     * @param defaultValue the default value to return if the feature state is unknown
     * @return true if enabled, false if disabled, or the default value if the state is unknown
     */
    fun isEnabled(defaultValue: Boolean): Boolean {
        return isEnabled() ?: defaultValue
    }

    /**
     * Check if the main feature flag is enabled, with a default value.
     *
     * @param defaultValue the default value to return if the feature state is unknown
     * @return true if enabled, false if disabled, or the default value if the state is unknown
     */
    fun isEnabled(defaultProvider: () -> Boolean): Boolean {
        return isEnabled() ?: defaultProvider()
    }

    /**
     * Check if a sub-flag of the feature is enabled.
     *
     * @param name the name of the sub-flag
     * @return true if enabled, false if disabled, or null if the sub-flag state is unknown
     */
    fun isEnabled(name: String): Boolean?

    /**
     * Check if a sub-flag of the feature is enabled, with a default value.
     *
     * @param name the name of the sub-flag
     * @param defaultValue the default value to return if the sub-flag state is unknown
     * @return true if enabled, false if disabled, or the default value if the state is unknown
     */
    fun isEnabled(name: String, defaultValue: Boolean): Boolean {
        return isEnabled(name) ?: defaultValue
    }

    /**
     * Check if a sub-flag of the feature is enabled, with a default value.
     *
     * @param name the name of the sub-flag
     * @param defaultValue the default value to return if the sub-flag state is unknown
     * @return true if enabled, false if disabled, or the default value if the state is unknown
     */
    fun isEnabled(name: String, defaultProvider: () -> Boolean): Boolean {
        return isEnabled(name) ?: defaultProvider()
    }

    fun ifEnabled(defaultValue: Boolean = false, action: () -> Unit) {
        if (isEnabled(defaultValue)) {
            action()
        }
    }

    fun ifEnabled(flag: String, defaultValue: Boolean = false, action: () -> Unit) {
        if (isEnabled(flag, defaultValue)) {
            action()
        }
    }

    fun getMetadata(name: String): MetadataValue? {
        return getMetadata(name, MetadataValue::class)
    }

    /**
     * Get a metadata value for the feature.
     *
     * @param name the name of the metadata value
     * @param type the type of the metadata value
     * @return the [MetadataValue] instance, or null if it does not exist or has a different type
     */
    fun <T : MetadataValue> getMetadata(name: String, type: KClass<T>): T?

    /**
     * Get a metadata value for the feature, with a default value.
     *
     * @param name the name of the metadata value
     * @param type the type of the metadata value
     * @param defaultValue a function providing the default value
     * @return the [MetadataValue] instance, or the default value if it does not exist or has a different type
     */
    fun <T : MetadataValue> getMetadata(name: String, type: KClass<T>, defaultValue: () -> T): T {
        return getMetadata(name, type) ?: defaultValue()
    }

    companion object {
        /**
         * Create a [FeatureOperations] instance from a [Feature] state.
         *
         * @param feature the feature state to create operations for
         * @return a [FeatureOperations] instance
         */
        fun of(feature: Feature): FeatureOperations = Api(feature)
    }

    /**
     * Implementation of [FeatureOperations] that uses a [Feature] state.
     */
    class Api(
        val feature: Feature
    ) : FeatureOperations {
        /**
         * Check if the main feature flag is enabled.
         *
         * @return true if enabled, false otherwise
         */
        override fun isEnabled(): Boolean {
            return feature.enabled
        }

        /**
         * Check if a sub-flag is enabled.
         *
         * @param name the name of the sub-flag
         * @return true if enabled, false otherwise
         */
        override fun isEnabled(name: String): Boolean? {
            val flagValue = feature.flags[name] ?: return null
            if (!feature.enabled) return false
            return flagValue
        }

        /**
         * Get a metadata value.
         *
         * @param name the name of the metadata value
         * @param type the type of the metadata value
         * @return the [MetadataValue] instance, or null if not found
         * @throws ClassCastException if the metadata value is found but of a different type
         */
        override fun <T : MetadataValue> getMetadata(
            name: String,
            type: KClass<T>
        ): T? {
            val unchecked = feature.metadata[name] ?: return null
            if (type.isInstance(unchecked)) {
                return unchecked as T
            } else {
                throw ClassCastException("Expected metadata $name to be of type ${type.qualifiedName}, but was of type ${unchecked::class.qualifiedName}")
            }
        }
    }

    /**
     * Implementation of [FeatureOperations] for unknown features.
     *
     * This implementation always returns null or the default value.
     */
    object Unknown : FeatureOperations {
        override fun isEnabled(): Boolean? {
            return null
        }

        override fun isEnabled(name: String): Boolean? {
            return null
        }

        override fun <T : MetadataValue> getMetadata(
            name: String,
            type: KClass<T>
        ): T? {
            return null
        }
    }
}

inline fun <reified T : MetadataValue> FeatureOperations.getMetadata(name: String): T? {
    return getMetadata(name, T::class)
}

inline fun <reified T : MetadataValue> FeatureOperations.getMetadata(name: String, noinline defaultValue: () -> T): T {
    return getMetadata(name, T::class, defaultValue)
}