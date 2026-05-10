package com.vivid.sdk;

import com.vivid.clients.api.Feature;

import java.util.List;

/**
 * Main entry point for the Vivid SDK.
 * <p>
 * This interface provides access to feature states and feature references.
 * It is typically used to query the state of a feature flag or to get a reference that can be subscribed to.
 */
public interface Features {

    static Features cacheBased(FeatureCache cache) {
        return new CacheBasedFeatures(cache);
    }

    /**
     * Get the operations for a specific feature key.
     * <p>
     * This is a "one-time" operation that returns a [FeatureOperations] instance.
     * Subsequent calls to this method can return the different instances.
     * If the underlying cache changed, the returned [FeatureOperations] instance may reflect the new state.
     * <p>
     * The returned instance is thread-safe and will not change if the underlying cache is modified.
     * If you require an adapting instance of the feature that is automatically updated, use [reference] instead.
     * <p>
     * Can be [FeatureOperations.Unknown] is the feature is not known at the time of the call.
     *
     * @param key the feature key to get operations for
     * @return a [FeatureOperations] instance for the given key
     */
    FeatureOperations get(String key);

    /**
     * Get a reference to a specific feature key.
     * <p>
     * A reference can be used to observe changes to a feature or to get the current state.
     * Whenever the underlying cache changes, the reference will reflect the new state.
     * <p>
     * Will never return [FeatureOperations.Unknown] and instead fall back to lazily evalute the flag state whenever needed.
     * If the flag is not known at that time, the [FeatureReference] will return null.
     *
     * @param key the feature key to get a reference for
     * @return a [FeatureReference] instance for the given key
     */
    FeatureReference reference(String key);

    /**
     * Returns all features known to this Feature instance.
     *
     * @return an immutable List all features known to this Feature instance.
     */
    List<Feature> getAll();

}
