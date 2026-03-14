package com.vivid.sdk

interface Features {

    fun get(key: String): FeatureOperations

    fun reference(key: String): FeatureReference

}