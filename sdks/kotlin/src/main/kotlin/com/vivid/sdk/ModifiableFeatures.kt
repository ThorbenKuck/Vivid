package com.vivid.sdk

import com.vivid.sdk.api.Feature

/**
 * A [Features] instance that allows modifying the internal state.
 *
 * This interface is typically used by [FeatureStream] implementations to update the local state when a remote update is received.
 */
interface ModifiableFeatures: Features {

    /**
     * Update the state of a single feature.
     *
     * @param feature the new feature state
     */
    fun set(feature: Feature)

    /**
     * Update the state of multiple features at once.
     *
     * @param features the new feature states
     */
    fun setAll(features: List<Feature>)

}
