package com.vivid.sdk.spring

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the Vivid SDK.
 *
 * @property environment the environment name to fetch features for
 * @property applicationId the optional application ID to send with requests
 * @property enabled whether the Vivid SDK is enabled
 * @property autostartStreams whether to automatically start the enabled streams on application startup
 */
@ConfigurationProperties("spring.vivid")
data class VividProperties(
    /**
     * Whether the Vivid SDK is enabled.
     */
    val enabled: Boolean,
    /**
     * Which environment the application is running in.
     */
    val environment: String,
    /**
     * The name of the application (use for metrics).
     */
    val applicationId: String?,
    val streams: Set<String>,
    val autostartStreams: Boolean,
)
