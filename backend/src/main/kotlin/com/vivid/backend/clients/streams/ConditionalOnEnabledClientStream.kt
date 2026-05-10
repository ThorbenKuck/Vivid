package com.vivid.backend.clients.streams

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.core.env.getProperty
import org.springframework.core.type.AnnotatedTypeMetadata

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(ConditionalOnEnabledClientStreamCondition::class)
annotation class ConditionalOnEnabledClientStream(
    val value: String = ""
)

class ConditionalOnEnabledClientStreamCondition : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata
    ): Boolean {
        val backplanesEnabled = context.environment.getProperty<Boolean>("application.clients.streams.enabled", true)
        if (!backplanesEnabled) {
            return false
        }

        val attributes = metadata.getAllAnnotationAttributes(
            ConditionalOnEnabledClientStream::class.qualifiedName!!
        )

        val streamNames = attributes
            ?.get("value")
            ?.filterIsInstance<String>()
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            .orEmpty()

        return streamNames.all { stream ->
            context.environment.getProperty<Boolean>(
                "application.clients.streams.$stream.enabled",
                false
            )
        }
    }
}