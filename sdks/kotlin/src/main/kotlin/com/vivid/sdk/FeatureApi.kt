package com.vivid.sdk

import com.vivid.sdk.api.Feature

/**
 * Interface for fetching feature states from a remote source.
 *
 * This interface is typically used by the [FeatureCache] to fetch feature states from the Vivid backend.
 * Implementations of this interface should handle the communication with the Vivid backend.
 *
 * For example, a RestFeatureApi might fetch feature states via HTTP.
 */
interface FeatureApi {

    /**
     * Fetch the state of a specific feature key.
     *
     * @param key the feature key to fetch the state for
     * @return the [Feature] state for the given key, or null if it could not be fetched
     */
    fun fetchFeature(key: String): Feature?

    /**
     * Fetch the states of all features.
     *
     * @return a list of [Feature] states, or null if they could not be fetched
     */
    fun fetchAllFeatures(): List<Feature>?

}
