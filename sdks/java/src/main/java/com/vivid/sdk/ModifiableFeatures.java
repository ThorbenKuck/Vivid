package com.vivid.sdk;

import com.vivid.clients.api.Feature;

import java.util.List;

/**
 * A [Features] instance that allows modifying the internal state.
 *
 * When you are providing a custom {@link Features} implementation, it is recommended to extend this interface.
 * Semantically, this interface is similar to [Features], but it allows modifying the internal state.
 * It is typically used by [FeatureStream] implementations to update the local state when a remote update is received.
 */
public interface ModifiableFeatures extends Features {
    /**
     * Update the state of a single feature.
     *
     * @param feature the new feature state
     */
    void set(Feature feature);

    /**
     * Update the state of multiple features at once.
     *
     * @param features the new feature states
     */
    void setAll(List<Feature> features);

}
