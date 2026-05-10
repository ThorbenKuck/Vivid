package com.vivid.sdk;

import com.vivid.clients.api.Feature;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * Interface for fetching feature states from a remote source.
 * <p>
 * This interface is typically used by the [FeatureCache] to fetch feature states from the Vivid backend.
 * Implementations of this interface should handle the communication with the Vivid backend.
 * <p>
 * For example, a RestFeatureApi might fetch feature states via HTTP.
 */
public interface FeatureApi {
    /**
     * Fetch the state of a specific feature key.
     *
     * @param key the feature key to fetch the state for
     * @return the [Feature] state for the given key, or null if it could not be fetched
     */
    @Nullable
    Feature fetchFeature(String key);

    /**
     * Fetch the states of all features.
     *
     * @return a list of [Feature] states, or null if they could not be fetched
     */
    @Nullable
    List<Feature> fetchAllFeatures();
}
