package com.vivid.sdk

import com.vivid.sdk.api.Feature
import com.vivid.sdk.api.MetadataValue
import kotlin.reflect.KClass

interface FeatureOperations {

    fun isEnabled(): Boolean?

    fun isEnabled(defaultValue: Boolean): Boolean {
        return isEnabled() ?: defaultValue
    }

    fun isEnabled(name: String): Boolean?

    fun isEnabled(name: String, defaultValue: Boolean): Boolean {
        return isEnabled(name) ?: defaultValue
    }

    fun <T: MetadataValue> getMetadata(name: String, type: KClass<T>): T?

    fun <T: MetadataValue> getMetadata(name: String, type: KClass<T>, defaultValue: () -> T): T {
        return getMetadata(name, type) ?: defaultValue()
    }

    companion object {
        fun of(feature: Feature): FeatureOperations = Api(feature)
    }

    class Api(
        val feature: Feature
    ): FeatureOperations {
        override fun isEnabled(): Boolean {
            return feature.enabled
        }

        override fun isEnabled(name: String): Boolean? {
            return feature.flags[name]
        }

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

    class Unknown: FeatureOperations {
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

inline fun <reified T: MetadataValue> FeatureOperations.getMetadata(name: String): T? {
    return getMetadata(name, T::class)
}

inline fun <reified T: MetadataValue> FeatureOperations.getMetadata(name: String, noinline defaultValue: () -> T): T {
    return getMetadata(name, T::class, defaultValue)
}