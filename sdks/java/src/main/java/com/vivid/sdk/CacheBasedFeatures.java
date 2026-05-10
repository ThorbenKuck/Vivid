package com.vivid.sdk;

import com.vivid.clients.api.Feature;

import java.util.List;

/**
 * A [ModifiableFeatures] implementation that is backed by a [FeatureCache].
 *
 * This implementation delegates all operations to the underlying cache.
 */
public class CacheBasedFeatures implements ModifiableFeatures {

    private final FeatureCache cache;

    public CacheBasedFeatures(FeatureCache cache) {
        this.cache = cache;
    }

    @Override
    public void set(Feature feature) {
        cache.set(feature);
    }

    @Override
    public void setAll(List<Feature> features) {
        cache.setAll(features);
    }

    @Override
    public FeatureOperations get(String key) {
        return FeatureOperations.of(cache.get(key));
    }

    @Override
    public FeatureReference reference(String key) {
        return FeatureReference.of(key, cache);
    }

    @Override
    public List<Feature> getAll() {
        return cache.getAll();
    }
}
