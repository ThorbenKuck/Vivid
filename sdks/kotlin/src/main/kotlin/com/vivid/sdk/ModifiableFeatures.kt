package com.vivid.sdk

import com.vivid.sdk.api.Feature

interface ModifiableFeatures: Features {

    fun set(feature: Feature)

    fun setAll(features: List<Feature>)

}
