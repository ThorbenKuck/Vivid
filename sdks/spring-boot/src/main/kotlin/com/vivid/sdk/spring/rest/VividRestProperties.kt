package com.vivid.sdk.spring.rest

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.vivid.rest")
data class VividRestProperties(
    val baseUrl: String,
    val enabled: Boolean = true,
    val apiToken: String? = null,
    val applicationIdHeaderName: String,
    val apiTokenHeaderName: String,
)
