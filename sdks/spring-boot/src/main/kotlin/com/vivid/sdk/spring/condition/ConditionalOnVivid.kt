package com.vivid.sdk.spring.condition

import org.springframework.context.annotation.Conditional

/**
 * Conditional annotation that matches if the Vivid SDK is enabled.
 *
 * If a value is provided, it also matches if the specific stream is enabled.
 *
 * @property value the names of the streams to check (optional)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(OnEnabledVividFeature::class)
annotation class ConditionalOnVivid(
    vararg val value: String
)
