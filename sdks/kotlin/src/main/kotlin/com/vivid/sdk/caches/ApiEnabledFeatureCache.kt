package com.vivid.sdk.caches

import com.vivid.sdk.FeatureApi
import com.vivid.sdk.api.Feature

/**
 * A simple wrapper around [SimpleFeatureCache] that passes the API as the miss handler.
 */
class ApiEnabledFeatureCache(
    private val api: FeatureApi,
    enabled: Boolean = true,
) : SimpleFeatureCache(
    enabled = enabled,
    missHandler = { api.fetchFeature(it) ?: Feature.empty() }
)
