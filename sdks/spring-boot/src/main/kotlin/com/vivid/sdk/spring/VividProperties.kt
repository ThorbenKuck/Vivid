package com.vivid.sdk.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties for the Vivid SDK.
 *
 * @property environment the environment name to fetch features for
 * @property applicationName the optional application ID to send with requests
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
     * The name of the application.
     */
    val applicationName: String?,
    /**
     * The token assigned to this client.
     *
     * Must match the token assigned at vivid.
     */
    val clientToken: String?,
    /**
     * All streams that are enabled for this client.
     */
    val streams: Set<String>,
    val autostartStreams: Boolean,
    val cache: Cache = Cache(),
) {
    data class Cache(
        val maxCapacity: Int = 1000,
        val retentionTime: Duration = Duration.ofMinutes(60),
    )
}
