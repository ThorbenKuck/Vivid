package com.vivid.backend.backplane

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.env.getProperty
import org.springframework.core.type.AnnotatedTypeMetadata

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(ConditionalOnEnabledBackplaneCondition::class)
annotation class ConditionalOnEnabledBackplane(
    val value: String = ""
)

class ConditionalOnEnabledBackplaneCondition : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata
    ): Boolean {
        val backplanesEnabled = context.environment.getProperty<Boolean>("application.backplane.enabled", true)
        if (!backplanesEnabled) {
            return false
        }

        val attributes = metadata.getAllAnnotationAttributes(
            ConditionalOnEnabledBackplane::class.qualifiedName!!
        )

        val backplaneNames = attributes
            ?.get("value")
            ?.filterIsInstance<String>()
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            .orEmpty()

        return backplaneNames.all { backplaneName ->
            context.environment.getProperty<Boolean>(
                "application.backplane.$backplaneName.enabled",
                false
            )
        }
    }
}