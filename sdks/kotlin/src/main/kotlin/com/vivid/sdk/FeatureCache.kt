package com.vivid.sdk

import com.vivid.sdk.api.Feature
import com.vivid.sdk.caches.ApiEnabledFeatureCache
import com.vivid.sdk.caches.SimpleFeatureCache

/**
 * Interface for a local cache of feature flag states.
 *
 * The cache is responsible for storing and retrieving feature flag states.
 * It also supports subscriptions to update events.
 */
interface FeatureCache {

    companion object {
        /**
         * Utility for creating a [FeatureCache] instance.
         *
         * The cache implementation by this factory method defaults to a simple in-memory cache.
         */
        operator fun invoke(): FeatureCache {
            return SimpleFeatureCache()
        }

        /**
         * Utility for create a [FeatureCache] instance that fetches feature flag states from an API.
         *
         * The cache implementation by this factory method is a wrapper around a [FeatureApi] instance.
         */
        operator fun invoke(api: FeatureApi): FeatureCache {
            return ApiEnabledFeatureCache(api)
        }
    }

    /**
     * Get the state of a specific feature.
     *
     * @param key the feature key
     * @return the [Feature] state, or null if not in cache
     */
    fun get(key: String): Feature?

    /**
     * Get all features in the cache
     *
     * @return a list of [Feature] states
     */
    fun getAll(): List<Feature>

    /**
     * Set the state of a specific feature.
     *
     * @param feature the new feature state
     * @return the previous [Feature] state, or null if it was not in cache
     */
    fun set(feature: Feature): Feature?

    /**
     * Registers an alternative name for the feature
     *
     * This is useful if you are referencing a feature not through the unique UUID, but through the key, name, or id.
     * The provided alias can be used interchangeably with the feature's UUID, key, or name.
     *
     * @param alias the alias to register for the feature
     * @param feature the feature to register the alias for
     */
    fun setAlias(alias: String, feature: Feature)

    /**
     * Set the states of multiple features at once.
     *
     * @param features the new feature states
     */
    fun setAll(features: List<Feature>)

    /**
     * Subscribe to updates for a specific feature.
     *
     * @param key the feature key to subscribe to
     * @param callback the callback to notify when the feature is updated or removed
     * @return a [Subscription] instance
     */
    fun subscribe(key: String, callback: Callback): Subscription

    /**
     * Remove a feature from the cache.
     *
     * @param key the feature key to remove
     * @return the removed [Feature] state, or null if it was not in cache
     */
    fun invalidate(key: String): Feature?

    /**
     * Invalidates the whole cache.
     *
     * This is the same as calling [invalidate] for all keys in the cache.
     */
    fun invalidate()

    /**
     * Sets the enabled state of this cache.
     *
     * If a cache is disabled, all operations will be no-op.
     *
     * @param state the new enabled state
     */
    fun enable(state: Boolean)

    /**
     * Callback for receiving updates from a [FeatureCache].
     */
    interface Callback {
        /**
         * Called when a feature is updated.
         *
         * @param feature the new feature state
         */
        fun onNext(feature: Feature)

        /**
         * Called when a feature is removed from the cache.
         *
         * @param feature the removed feature state
         */
        fun onRemove(feature: Feature) {
        }
    }
}
