package com.vivid.sdk

import com.vivid.sdk.api.Feature

interface FeatureApi {

    fun fetchFeature(key: String): Feature?

    fun fetchAllFeatures(): List<Feature>?

}
