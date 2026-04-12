package com.vivid.sdk.spring.rest

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the REST-based feature fetching.
 *
 * @property baseUrl the base URL of the Vivid backend
 * @property enabled whether the REST fetching is enabled
 * @property apiToken the API token to use for authentication
 * @property applicationIdHeaderName the header name for the application ID
 * @property apiTokenHeaderName the header name for the API token
 */
@ConfigurationProperties("spring.vivid.rest")
data class VividRestProperties(
    val enabled: Boolean = true,
    val baseUrl: String,
    val apiToken: String? = null,
    val applicationIdHeaderName: String,
    val apiTokenHeaderName: String,
)
