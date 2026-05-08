package com.vivid.sdk.spring.condition

import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * Condition that matches if the required feature stream is enabled.
 */
class OnEnabledFeatureStreamCondition : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata
    ): Boolean {
        val attributes = metadata.getAnnotationAttributes(ConditionalOnFeatureStream::class.java.name)
        val requiredStream = attributes?.get("value") as? String ?: error("Missing required attribute 'value' on @ConditionalOnEnabledStream")

        val enabledStreams: Set<String> = Binder.get(context.environment)
            .bind("spring.vivid.streams", Bindable.setOf(String::class.java))
            .orElse(emptySet())
            ?: emptySet()

        return enabledStreams.contains(requiredStream)
    }
}