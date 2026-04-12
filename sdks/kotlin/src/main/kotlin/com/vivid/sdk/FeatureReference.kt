package com.vivid.sdk

import com.vivid.sdk.api.MetadataValue
import kotlin.reflect.KClass

/**
 * A reference to a specific feature flag.
 *
 * A reference can be used to check the current state of a feature and its sub-flags.
 * Contrary to a [FeatureOperations] instance, a [FeatureReference] is evaluated lazily.
 *
 * An implementation should not cache the result of the evaluation in its own state.
 * Instead, it should use the provided [FeatureCache] to store and retrieve feature states.
 * Alternative implementations could use remote API calls or other mechanisms.
 */
interface FeatureReference: FeatureOperations {

    /**
     * A [FeatureReference] implementation that is backed by a [FeatureCache].
     */
    class CacheBased(
        private val key: String,
        private val cache: FeatureCache,
    ): FeatureReference {
        override fun isEnabled(): Boolean? {
            return cache.get(key)?.enabled
        }

        override fun isEnabled(name: String): Boolean? {
            val cachedValue = cache.get(key) ?: return null
            if (!cachedValue.enabled) {
                return false
            }

            return cachedValue.flags[name]
        }

        override fun <T : MetadataValue> getMetadata(
            name: String,
            type: KClass<T>
        ): T? {
            return cache.get(key)?.checkedMetadataValue(name, type)
        }
    }
}
