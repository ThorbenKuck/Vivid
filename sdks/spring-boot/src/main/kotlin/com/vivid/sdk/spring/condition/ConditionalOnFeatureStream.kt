package com.vivid.sdk.spring.condition

import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(OnEnabledFeatureStreamCondition::class)
annotation class ConditionalOnFeatureStream(
    val value: String
)
