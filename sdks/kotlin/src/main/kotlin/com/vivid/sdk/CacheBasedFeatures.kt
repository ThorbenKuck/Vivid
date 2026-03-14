package com.vivid.sdk

import com.vivid.sdk.api.Feature

class CacheBasedFeatures(
    private val cache: FeatureCache
): ModifiableFeatures {

    override fun set(feature: Feature) {
        cache.set(feature)
    }

    override fun setAll(features: List<Feature>) {
        cache.setAll(features)
    }

    override fun get(key: String): FeatureOperations {
        return cache.get(key)?.let { FeatureOperations.of(it) } ?: FeatureOperations.Unknown()
    }

    override fun reference(key: String): FeatureReference {
        return FeatureReference.CacheBased(key, cache)
    }
}
