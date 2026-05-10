package com.vivid.sdk.caches;

import com.vivid.clients.api.Feature;
import com.vivid.sdk.FeatureApi;

import java.util.Objects;

/**
 * A simple wrapper around [SimpleFeatureCache] that passes the API as the miss handler.
 */
public class ApiEnabledFeatureCache extends InMemoryFeatureCache {

    public ApiEnabledFeatureCache(
            FeatureApi api
    ) {
        this(api, true);
    }

    public ApiEnabledFeatureCache(
            FeatureApi api,
            boolean enabled
    ) {
        super(enabled, (key) -> Objects.requireNonNullElse(api.fetchFeature(key), Feature.empty()));
    }

    public static InMemoryFeatureCache.Builder builder(FeatureApi api) {
        Builder builder = InMemoryFeatureCache.builder();
        builder.missHandler((key) -> Objects.requireNonNullElse(api.fetchFeature(key), Feature.empty()));
        return builder;
    }
}
