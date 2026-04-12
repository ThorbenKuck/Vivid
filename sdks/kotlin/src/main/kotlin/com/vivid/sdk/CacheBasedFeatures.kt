package com.vivid.sdk

import com.vivid.sdk.api.Feature

/**
 * A [ModifiableFeatures] implementation that is backed by a [FeatureCache].
 *
 * This implementation delegates all operations to the underlying cache.
 */
class CacheBasedFeatures(
    /**
     * The underlying [FeatureCache] instance.
     */
    private val cache: FeatureCache
): ModifiableFeatures {
    fun cache(): FeatureCache {
        return cache
    }

    override fun set(feature: Feature) {
        cache.set(feature)
    }

    override fun setAll(features: List<Feature>) {
        cache.setAll(features)
    }

    override fun get(key: String): FeatureOperations {
        return cache.get(key)?.let { FeatureOperations.of(it) } ?: FeatureOperations.Unknown
    }

    override fun reference(key: String): FeatureReference {
        return FeatureReference.CacheBased(key, cache)
    }
}
