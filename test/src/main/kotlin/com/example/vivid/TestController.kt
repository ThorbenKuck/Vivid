package com.example.vivid

import com.vivid.sdk.FeatureReference
import com.vivid.sdk.Features
import com.vivid.sdk.api.MetadataValue
import com.vivid.sdk.api.contains
import com.vivid.sdk.api.isFalse
import com.vivid.sdk.api.isTrue
import com.vivid.sdk.api.metadata.StringListMetadataValue
import com.vivid.sdk.api.metadata.StringMetadataValue
import com.vivid.sdk.getMetadata
import com.vivid.sdk.spring.qualifier.Vivid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val features: Features,
    @Vivid("Test")
    private val testfeature: FeatureReference,
) {

    @GetMapping("reference")
    fun reference(): String? {
        return testfeature.isEnabled()?.toString()
    }

    @GetMapping("/{feature}")
    fun onDemand(@PathVariable feature: String): String? {
        return features.get(feature).isEnabled()?.toString()
    }

    @GetMapping("/{feature}/{flag}")
    fun flag(@PathVariable feature: String, @PathVariable flag: String): String? {
        return features.get(feature).isEnabled(flag)?.toString()
    }

    @GetMapping("/{feature}/meta/{name}")
    fun meta(@PathVariable feature: String, @PathVariable name: String): Any? {
        return features.get(feature).getMetadata(name)
    }
}
