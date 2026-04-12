package com.vivid.sdk.caches

import com.vivid.sdk.FeatureApi
import com.vivid.sdk.api.Feature

/**
 * A [FeatureCache] implementation that fetches missing feature states from a [FeatureApi].
 *
 * This implementation is useful when not all feature states are pushed to the application.
 */
class FetchingFeatureCache(
    private val fetchFeature: FeatureApi
) : SimpleFeatureCache() {

    override fun get(key: String): Feature? {
        return content.computeIfAbsent(key) {
            fetchFeature.fetchFeature(key) ?: EMPTY_FEATURE
        }.nullIfEmpty()
    }
}