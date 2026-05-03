package com.vivid.sdk.spring

import com.vivid.sdk.FeatureCache
import com.vivid.sdk.caches.LayeredFeatureCache

class FeatureCacheManager(
    private val primaryCache: FeatureCache,
    private val panicCache: FeatureCache? = null,
) {

    companion object {
        fun buildRootCache(primaryCache: FeatureCache, panicCache: FeatureCache? = null): FeatureCache {
            return if (panicCache == null) {
                primaryCache
            } else {
                LayeredFeatureCache(listOf(primaryCache, panicCache))
            }
        }
    }

    private val rootCache = buildRootCache(primaryCache, panicCache)

    fun switchToPanicMode() {
        if (panicCache == null) {
            error("Panic mode is not possible: No panic cache configured!")
        }

        primaryCache.enable(false)
        primaryCache.invalidate()
        panicCache.enable(true)
    }

    fun switchToNormalMode() {
        primaryCache.enable(true)
        panicCache?.enable(false)
    }
}
