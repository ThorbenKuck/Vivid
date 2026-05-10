package com.vivid.sdk.caches;

import com.vivid.clients.api.Feature;
import com.vivid.sdk.FeatureCache;
import com.vivid.sdk.Subscription;

import java.util.Collections;
import java.util.List;

/**
 * A {@link FeatureCache} implementation that combines multiple cache layers.
 *
 * <p>Reads are performed from the first layer to the last layer. If a feature is found
 * in a lower layer, it is written back into all previous layers.</p>
 *
 * <p>Writes are propagated to all layers.</p>
 */
public class LayeredFeatureCache implements FeatureCache {

    private final List<FeatureCache> layers;
    private volatile boolean enabled;

    public LayeredFeatureCache(List<FeatureCache> layers) {
        this(layers, true);
    }

    public LayeredFeatureCache(List<FeatureCache> layers, boolean enabled) {
        this.layers = List.copyOf(layers);
        this.enabled = enabled;
    }

    @Override
    public Feature get(String key) {
        if (!enabled) {
            return null;
        }

        for (int i = 0; i < layers.size(); i++) {
            Feature feature = layers.get(i).get(key);

            if (feature != null) {
                // Backfill: write the value to all previous, probably faster cache layers.
                for (int previousIndex = 0; previousIndex < i; previousIndex++) {
                    FeatureCache previousLayer = layers.get(previousIndex);
                    previousLayer.set(feature);
                    previousLayer.setAlias(key, feature);
                }

                return feature;
            }
        }

        return null;
    }

    @Override
    public Feature set(Feature feature) {
        if (!enabled) {
            return null;
        }

        Feature previous = null;

        // Write-through: write into all layers.
        for (FeatureCache layer : layers) {
            Feature old = layer.set(feature);
            if (previous == null) {
                previous = old;
            }
        }

        return previous;
    }

    @Override
    public void setAlias(String alias, Feature feature) {
        if (!enabled) {
            return;
        }

        for (FeatureCache layer : layers) {
            layer.setAlias(alias, feature);
        }
    }

    @Override
    public void setAll(List<Feature> features) {
        if (!enabled) {
            return;
        }

        for (FeatureCache layer : layers) {
            layer.setAll(features);
        }
    }

    @Override
    public List<Feature> getAll() {
        if (!enabled) {
            return Collections.emptyList();
        }

        if (layers.isEmpty()) {
            return Collections.emptyList();
        }

        return layers.get(0).getAll();
    }

    @Override
    public Feature invalidate(String key) {
        if (!enabled) {
            return null;
        }

        Feature firstRemoved = null;

        for (FeatureCache layer : layers) {
            Feature removed = layer.invalidate(key);
            if (firstRemoved == null && removed != null) {
                firstRemoved = removed;
            }
        }

        return firstRemoved;
    }

    @Override
    public void invalidate() {
        if (!enabled) {
            return;
        }

        for (FeatureCache layer : layers) {
            layer.invalidate();
        }
    }

    @Override
    public Subscription subscribe(String key, FeatureCache.Callback callback) {
        if (layers.isEmpty()) {
            return new EmptySubscription();
        }

        return layers.getFirst().subscribe(key, callback);
    }

    @Override
    public void enable(boolean state) {
        this.enabled = state;
    }

    private static class EmptySubscription implements Subscription {

        @Override
        public void cancel() {
            // No-op
        }
    }
}