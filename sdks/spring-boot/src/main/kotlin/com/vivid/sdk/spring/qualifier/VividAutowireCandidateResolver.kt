package com.vivid.sdk.spring.qualifier

import com.vivid.sdk.FeatureOperations
import com.vivid.sdk.FeatureReference
import com.vivid.sdk.Features
import org.springframework.beans.factory.config.DependencyDescriptor
import org.springframework.beans.factory.support.AutowireCandidateResolver

class VividAutowireCandidateResolver(
    private val features: Features,
): AutowireCandidateResolver {
    override fun getSuggestedValue(descriptor: DependencyDescriptor): Any? {
        val vivid = descriptor.getAnnotation(Vivid::class.java)
        if (vivid != null) {
            if (descriptor.dependencyType == FeatureReference::class.java) {
                return features.reference(vivid.value)
            }
            if (descriptor.dependencyType == FeatureOperations::class.java) {
                return features.get(vivid.value)
            }
        }

        return super.getSuggestedValue(descriptor)

    }
}
