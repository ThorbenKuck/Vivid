package com.vivid.sdk.spring.sse

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the SSE-based feature streaming.
 *
 * @property baseUrl the base URL of the Vivid backend
 * @property enabled whether SSE streaming is enabled
 * @property apiKey the API key to use for authentication
 * @property applicationIdHeaderName the header name for the application ID
 * @property apiTokenHeaderName the header name for the API token
 */
@ConfigurationProperties("spring.vivid.sse")
data class VividSseProperties(
    val baseUrl: String,
    val enabled: Boolean = true,
    val apiKey: String? = null,
    val applicationIdHeaderName: String,
    val apiTokenHeaderName: String,
)
