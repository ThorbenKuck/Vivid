package com.vivid.sdk.caches

import com.vivid.sdk.FeatureCache
import com.vivid.sdk.Subscription
import com.vivid.sdk.api.Feature
import java.util.concurrent.ConcurrentHashMap

abstract class BaseFeatureCache: FeatureCache {

    private val subscriptions = ConcurrentHashMap<String, MutableList<FeatureCache.Callback>>()

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