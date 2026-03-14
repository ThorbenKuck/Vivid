package com.vivid.sdk.spring.condition

import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Conditional(OnEnabledVividFeature::class)
annotation class ConditionalOnVivid(
    vararg val value: String
)
