package com.vivid.sdk.caches

import com.vivid.sdk.FeatureCache
import com.vivid.sdk.Subscription
import com.vivid.sdk.api.Feature
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple implementation of [FeatureCache] that stores feature states in a [ConcurrentHashMap].
 *
 * This implementation is thread-safe and supports atomic updates.
 */
open class SimpleFeatureCache : FeatureCache {

    private val subscriptions = ConcurrentHashMap<String, MutableList<FeatureCache.Callback>>()
    protected val content = ConcurrentHashMap<String, Feature>()
    protected val writeLock = Any()

    companion object {
        @JvmStatic
        protected val EMPTY_FEATURE =
            Feature(
                id = "",
                enabled = false,
                flags = emptyMap(),
                metadata = emptyMap(),
                name = "",
                timestamp = Instant.ofEpochMilli(0)
            )
    }

    override fun get(key: String): Feature? {
        return content[key]
    }

    override fun invalidate(key: String): Feature? {
        return content.remove(key)?.also { notifySubscribersAboutRemove(it) }
    }

    protected fun Feature?.nullIfEmpty(): Feature? = this?.takeIf { it !== EMPTY_FEATURE }

    protected fun updateIfNewer(feature: Feature): Pair<Feature?, Feature?> {
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

    override fun set(feature: Feature): Feature? {
        // Diese Methode bleibt für Einzel-Updates erhalten,
        // nutzt aber intern die neue Logik ohne sofortiges Blockieren,
        // falls wir sie von setAll aus aufrufen wollen.
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

    override fun getAll(): List<Feature> {
        return content.values.toList()
    }

    protected fun notifySubscribersAboutNext(feature: Feature) {
        subscriptions[feature.name]?.forEach { it.onNext(feature) }
    }

    protected fun notifySubscribersAboutRemove(feature: Feature) {
        subscriptions[feature.name]?.forEach { it.onRemove(feature) }
    }

    override fun subscribe(
        key: String,
        callback: FeatureCache.Callback
    ): Subscription {
        subscriptions.computeIfAbsent(key) { mutableListOf() }.add(callback)
        return SubscriptionImpl(callback, key)
    }

    inner class SubscriptionImpl(
        private val callback: FeatureCache.Callback,
        private val key: String,
    ) : Subscription {
        override fun cancel() {
            subscriptions.computeIfPresent(key) { _, list -> list.remove(callback); list.ifEmpty { null } }
        }
    }
}