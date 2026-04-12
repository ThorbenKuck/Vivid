package com.vivid.sdk

/**
 * Main entry point for the Vivid SDK.
 *
 * This interface provides access to feature states and feature references.
 * It is typically used to query the state of a feature flag or to get a reference that can be subscribed to.
 */
interface Features {

    companion object {
        operator fun invoke(cache: FeatureCache) = CacheBasedFeatures(cache)
    }

    /**
     * Get the operations for a specific feature key.
     *
     * This is a "one-time" operation that returns a [FeatureOperations] instance.
     * Subsequent calls to this method can return the different instances.
     * If the underlying cache changed, the returned [FeatureOperations] instance may reflect the new state.
     *
     * The returned instance is thread-safe and will not change if the underlying cache is modified.
     * If you require an adapting instance of the feature that is automatically updated, use [reference] instead.
     *
     * Can be [FeatureOperations.Unknown] is the feature is not known at the time of the call.
     *
     * @param key the feature key to get operations for
     * @return a [FeatureOperations] instance for the given key
     */
    fun get(key: String): FeatureOperations

    /**
     * Get a reference to a specific feature key.
     *
     * A reference can be used to observe changes to a feature or to get the current state.
     * Whenever the underlying cache changes, the reference will reflect the new state.
     *
     * Will never return [FeatureOperations.Unknown] and instead fall back to lazily evalute the flag state whenever needed.
     * If the flag is not known at that time, the [FeatureReference] will return null.
     *
     * @param key the feature key to get a reference for
     * @return a [FeatureReference] instance for the given key
     */
    fun reference(key: String): FeatureReference

}