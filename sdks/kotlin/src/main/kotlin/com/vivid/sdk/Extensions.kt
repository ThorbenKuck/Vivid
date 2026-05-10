package com.vivid.sdk

import com.vivid.clients.api.Feature
import com.vivid.clients.api.MetadataValue
import com.vivid.sdk.caches.InMemoryFeatureCache

fun newFeature(consumer: Feature.Builder.() -> Unit): Feature {
    return Feature.builder().apply(consumer).build()
}

fun newInMemoryFeatureCache(consumer: InMemoryFeatureCache.Builder.() -> Unit): InMemoryFeatureCache {
    return InMemoryFeatureCache.builder().apply(consumer).build()
}

inline fun <reified T : MetadataValue<Any>> FeatureOperations.getMetadata(name: String): T? {
    return getMetadata(name, T::class.java)
}

inline fun <reified T : MetadataValue<Any>> FeatureOperations.getMetadata(name: String, noinline defaultValue: () -> T): T {
    return getMetadata(name, T::class.java, defaultValue)
}