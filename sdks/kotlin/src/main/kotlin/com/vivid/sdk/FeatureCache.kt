package com.vivid.sdk

import com.vivid.sdk.api.Feature

interface FeatureCache {

    fun get(key: String): Feature?

    /**
     * Sets the feature in the cache and returns the previous value.
     */
    fun set(feature: Feature): Feature?

    fun setAll(features: List<Feature>)

    fun subscribe(key: String, callback: Callback): Subscription

    fun invalidate(key: String): Feature?

    interface Callback {
        fun onNext(feature: Feature)

        fun onRemove(feature: Feature) {

        }
    }
}
