package com.vivid.sdk.spring.sse

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.vivid.sse")
data class VividSseProperties(
    val baseUrl: String,
    val enabled: Boolean = true,
    val apiKey: String? = null,
    val applicationIdHeaderName: String,
    val apiTokenHeaderName: String,
)
