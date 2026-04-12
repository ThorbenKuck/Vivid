package com.vivid.sdk.spring.condition

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.getProperty
import org.springframework.core.type.AnnotatedTypeMetadata
import kotlin.jvm.java

/**
 * Condition that matches if the Vivid SDK is enabled.
 */
class OnEnabledVividFeature: Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata
    ): Boolean {
        if (!context.environment.getProperty<Boolean>("spring.vivid.enabled", true)) {
            return false
        }

        val metadata = metadata.getAnnotationAttributes(ConditionalOnVivid::class.java.name)
            ?: error("Missing required annotation @ConditionalOnVivid")

        metadata["value"]?.let { vivids ->
            (vivids as Array<String>).forEach { vivid ->
                if (!context.environment.getProperty<Boolean>("spring.vivid.$vivid.enabled", true)) {
                    return false
                }
            }
        }

        return true
    }
}