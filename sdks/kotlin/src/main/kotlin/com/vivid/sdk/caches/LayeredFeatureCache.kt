package com.vivid.sdk.caches

import com.vivid.sdk.FeatureCache
import com.vivid.sdk.Subscription
import com.vivid.sdk.api.Feature

class LayeredFeatureCache(
    private val layers: List<FeatureCache>,
    @Volatile var enabled: Boolean = true
) : FeatureCache {

    override fun get(key: String): Feature? {
        if (!enabled) return null

        for (i in layers.indices) {
            val feature = layers[i].get(key)
            if (feature != null) {
                // "Backfill": Den Wert in alle darüberliegenden (wahrscheinlich schnelleren) Caches schreiben
                for (prev in 0 until i) {
                    layers[prev].set(feature)
                    layers[prev].setAlias(key, feature)
                }

                return feature
            }
        }
        return null
    }

    override fun set(feature: Feature): Feature? {
        if (!enabled) return null

        var previous: Feature? = null
        // Write-Through: In alle Ebenen schreiben
        layers.forEach { layer ->
            val old = layer.set(feature)
            if (previous == null) previous = old
        }
        return previous
    }

    override fun setAlias(alias: String, feature: Feature) {
        if (!enabled) return

        layers.forEach {
            it.setAlias(alias, feature)
        }
    }

    override fun setAll(features: List<Feature>) {
        if (!enabled) return
        layers.forEach { it.setAll(features) }
    }

    override fun getAll(): List<Feature> {
        if (!enabled) return emptyList()
        return layers.firstOrNull()?.getAll() ?: emptyList()
    }

    override fun invalidate(key: String): Feature? {
        if (!enabled) return null
        return layers.map { it.invalidate(key) }.firstOrNull { it != null }
    }

    override fun invalidate() {
        if (!enabled) return
        layers.forEach { it.invalidate() }
    }

    override fun subscribe(key: String, callback: FeatureCache.Callback): Subscription {
        return layers.firstOrNull()?.subscribe(key, callback) ?: Subscription.Empty
    }

    override fun enable(state: Boolean) {
        this.enabled = state
    }
}
