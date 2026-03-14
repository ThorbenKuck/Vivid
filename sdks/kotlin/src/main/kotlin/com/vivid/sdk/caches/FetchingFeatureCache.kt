package com.vivid.sdk.caches

import com.vivid.sdk.FeatureApi
import com.vivid.sdk.api.Feature
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class FetchingFeatureCache(
    private val fetchFeature: FeatureApi
) : BaseFeatureCache() {

    companion object {
        private val EMPTY_FEATURE =
            Feature(
                id = "",
                enabled = false,
                flags = emptyMap(),
                metadata = emptyMap(),
                name = "",
                timestamp = Instant.ofEpochMilli(0)
            )
    }

    private val writeLock = Any()
    private val content = ConcurrentHashMap<String, Feature>()

    override fun get(key: String): Feature? {
        return content.computeIfAbsent(key) {
            fetchFeature.fetchFeature(key) ?: EMPTY_FEATURE
        }.nullIfEmpty()
    }

    private fun Feature?.nullIfEmpty(): Feature? = this?.takeIf { it !== EMPTY_FEATURE }

    override fun set(feature: Feature): Feature? {
        return synchronized(writeLock) {
            val (previous, updatedFeature) = updateIfNewer(feature)
            if (updatedFeature != null) {
                notifySubscribersAboutNext(updatedFeature)
            }
            previous
        }
    }

    override fun setAll(features: List<Feature>) {
        val newFeaturesMap = features.associateBy { it.name }
        val newKeys = newFeaturesMap.keys
        val updatedForNotification = mutableListOf<Feature>()

        synchronized(writeLock) {
            features.forEach { incoming ->
                val (_, updated) = updateIfNewer(incoming)
                if (updated != null) {
                    updatedForNotification.add(updated)
                }
            }

            content.keys.removeIf { it !in newKeys }
        }

        updatedForNotification.forEach {
            notifySubscribersAboutNext(it)
        }
    }

    private fun updateIfNewer(feature: Feature): Pair<Feature?, Feature?> {
        var previous: Feature? = null
        var updated: Feature? = null

        content.compute(feature.name) { _, existing ->
            if (existing == null || feature.timestamp.isAfter(existing.timestamp)) {
                previous = existing
                updated = feature
                feature
            } else {
                updated = null
                existing
            }
        }
        return previous to updated
    }

    override fun invalidate(key: String): Feature? {
        return content.remove(key)?.also { notifySubscribersAboutRemove(it) }
    }
}