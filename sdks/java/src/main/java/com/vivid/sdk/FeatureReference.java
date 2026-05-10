package com.vivid.sdk;

import com.vivid.clients.api.Feature;
import com.vivid.clients.api.MetadataValue;
import jakarta.annotation.Nullable;

/**
 * A reference to a specific feature flag.
 * <p>
 * A reference can be used to check the current state of a feature and its sub-flags.
 * Contrary to a [FeatureOperations] instance, a [FeatureReference] is evaluated lazily.
 * <p>
 * An implementation should not cache the result of the evaluation in its own state.
 * Instead, it should use the provided [FeatureCache] to store and retrieve feature states.
 * Alternative implementations could use remote API calls or other mechanisms.
 */
public interface FeatureReference extends FeatureOperations {

    static FeatureReference of(String key, FeatureCache cache) {
        return new CacheBased(key, cache);
    }

    class CacheBased implements FeatureReference {

        private final String key;
        private final FeatureCache cache;

        public CacheBased(String key, FeatureCache cache) {
            this.key = key;
            this.cache = cache;
        }

        @Nullable
        @Override
        public Boolean isEnabled() {
            Feature feature = cache.get(key);
            if (feature == null) return null;
            return feature.enabled();
        }

        @Nullable
        @Override
        public Boolean isEnabled(String name) {
            Feature feature = cache.get(key);
            if (feature == null) return null;
            Boolean featureFlag = feature.flags().get(name);
            if (featureFlag == null) return null;
            return feature.enabled() && featureFlag;

        }

        @Nullable
        @Override
        public <S, T extends MetadataValue<S>> T getMetadata(String name, Class<T> type) {
            Feature feature = cache.get(key);
            if (feature == null) return null;

            return feature.checkedMetadataValue(name, type);
        }
    }
}
