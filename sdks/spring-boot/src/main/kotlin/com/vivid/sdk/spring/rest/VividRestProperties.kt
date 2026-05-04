package com.vivid.sdk.spring.rest

import com.vivid.sdk.spring.HeaderNamesProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties for the REST-based feature fetching.
 *
 * @property baseUrl the base URL of the Vivid backend
 * @property enabled whether the REST fetching is enabled
 */
@ConfigurationProperties("spring.vivid.rest")
data class VividRestProperties(
    val enabled: Boolean = true,
    val baseUrl: String,
    val fetchOnMiss: Boolean = true,
    val heartbeat: Heartbeat = Heartbeat(),
    val headerNames: HeaderNamesProperties = HeaderNamesProperties(),
) {
    data class Heartbeat(
        val enabled: Boolean = true,
        val interval: Duration = Duration.ofMinutes(5),
    )
}
