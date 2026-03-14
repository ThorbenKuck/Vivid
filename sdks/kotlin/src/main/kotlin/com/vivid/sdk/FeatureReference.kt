package com.vivid.sdk

import com.vivid.sdk.api.MetadataValue
import kotlin.reflect.KClass

interface FeatureReference: FeatureOperations {

    class CacheBased(
        private val key: String,
        private val cache: FeatureCache,
    ): FeatureReference {
        override fun isEnabled(): Boolean? {
            return cache.get(key)?.enabled
        }

        override fun isEnabled(name: String): Boolean? {
            return cache.get(key)?.flags?.get(name)
        }

        override fun <T : MetadataValue> getMetadata(
            name: String,
            type: KClass<T>
        ): T? {
            return cache.get(key)?.checkedMetadataValue(name, type)
        }
    }
}
