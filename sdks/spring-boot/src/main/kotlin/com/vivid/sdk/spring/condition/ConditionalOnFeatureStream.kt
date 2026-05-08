package com.vivid.sdk.spring.condition

import org.springframework.context.annotation.Conditional

/**
 * Conditional annotation that matches if a specific feature stream is enabled.
 *
 * @property value the name of the stream to check
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(OnEnabledFeatureStreamCondition::class)
annotation class ConditionalOnFeatureStream(
    val value: String
)
