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
open class SimpleFeatureCache(
    @Volatile var enabled: Boolean = true,
    private val missHandler: (String) -> Feature = { Feature.empty() },
    protected val writeLock: Any = Any()
) : FeatureCache {

    private val subscriptions = ConcurrentHashMap<String, MutableList<FeatureCache.Callback>>()
    protected val translations = ConcurrentHashMap<String, String>()
    protected val content = ConcurrentHashMap<String, Feature>()

    protected fun translate(key: String): String {
        return translations[key] ?: key
    }

    override fun setAlias(alias: String, feature: Feature) {
        translations[alias] = feature.id
    }

    override fun get(key: String): Feature? {
        if (!enabled) {
            return null
        }

        val result = content[translate(key)]
        if (result != null) {
            return result.nullIfEmpty()
        }

        val match = missHandler(key)
        set(match)
        translations[key] = match.id
        return match.nullIfEmpty()
    }

    override fun invalidate(key: String): Feature? {
        return if(enabled) content.remove(translate(key))?.also { notifySubscribersAboutRemove(it) } else null
    }

    override fun invalidate() {
        if (enabled) {
            ArrayList(content.keys().toList()).forEach { key -> invalidate(key) }
        }
    }

    override fun enable(state: Boolean) {
        this.enabled = state
    }

    protected fun updateIfNewer(feature: Feature): Pair<Feature?, Feature?> {
        if (!enabled) return Pair(null, null)

        var previous: Feature? = null
        var updated: Feature? = null

        content.compute(feature.id) { _, existing ->
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
        if (!enabled) return null

        return synchronized(writeLock) {
            val (previous, updatedFeature) = updateIfNewer(feature)
            if (updatedFeature != null) {
                notifySubscribersAboutNext(updatedFeature)
            }
            previous
        }
    }

    override fun setAll(features: List<Feature>) {
        if (!enabled) return

        val newFeaturesMap = features.associateBy { it.id }
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
        if (!enabled) return emptyList()
        return content.values.toList().filter { !it.isEmpty() }
    }

    protected fun notifySubscribersAboutNext(feature: Feature) {
        if (!enabled) return
        subscriptions[feature.name]?.forEach { it.onNext(feature) }
    }

    protected fun notifySubscribersAboutRemove(feature: Feature) {
        if (!enabled) return
        subscriptions[feature.name]?.forEach { it.onRemove(feature) }
    }

    /**
     * The subscription logic stays untouched to the original implementation.
     */
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