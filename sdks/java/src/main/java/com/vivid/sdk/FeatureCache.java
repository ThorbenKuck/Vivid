package com.vivid.sdk;

import com.vivid.clients.api.Feature;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * Interface for a local cache of feature flag states.
 * <p>
 * The cache is responsible for storing and retrieving feature flag states.
 * It also supports subscriptions to update events.
 */
public interface FeatureCache {

    /**
     * Get the state of a specific feature.
     *
     * @param key the feature key
     * @return the [Feature] state, or null if not in cache
     */
    @Nullable
    Feature get(String key);

    /**
     * Get all features in the cache
     *
     * @return an immutable list of [Feature] states
     */
    List<Feature> getAll();

    /**
     * Set the states of multiple features at once.
     *
     * @param features the new feature states
     */
    void setAll(List<Feature> features);

    /**
     * Set the state of a specific feature.
     *
     * @param feature the new feature state
     * @return the previous [Feature] state, or null if it was not in cache
     */
    @Nullable
    Feature set(Feature feature);

    /**
     * Registers an alternative name for the feature
     * <p>
     * This is useful if you are referencing a feature not through the unique UUID, but through the key, name, or id.
     * The provided alias can be used interchangeably with the feature's UUID, key, or name.
     *
     * @param alias   the alias to register for the feature
     * @param feature the feature to register the alias for
     */
    void setAlias(String alias, Feature feature);

    /**
     * Subscribe to updates for a specific feature.
     *
     * @param key      the feature key to subscribe to
     * @param callback the callback to notify when the feature is updated or removed
     * @return a [Subscription] instance
     */
    Subscription subscribe(String key, Callback callback);

    /**
     * Remove a feature from the cache.
     *
     * @param key the feature key to remove
     * @return the removed [Feature] state, or null if it was not in cache
     */
    @Nullable
    Feature invalidate(String key);

    /**
     * Invalidates the whole cache.
     * <p>
     * This is the same as calling [invalidate] for all keys in the cache.
     */
    void invalidate();

    /**
     * Sets the enabled state of this cache.
     * <p>
     * If a cache is disabled, all operations will be no-op.
     *
     * @param state the new enabled state
     */
    void enable(boolean state);

    /**
     * Callback for receiving updates from a [FeatureCache].
     */
    interface Callback {
        /**
         * Called when a feature is updated.
         *
         * @param feature the new feature state
         */
        void onNext(Feature feature);

        /**
         * Called when a feature is removed from the cache.
         *
         * @param feature the removed feature state
         */
        default void onRemove(Feature feature) {
        }
    }
}
