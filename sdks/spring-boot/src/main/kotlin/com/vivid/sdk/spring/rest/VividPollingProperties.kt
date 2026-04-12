package com.vivid.sdk.spring.rest

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Configuration properties for the polling feature stream.
 *
 * @property enabled whether polling is enabled
 * @property interval the polling interval (default: 60m)
 */
@ConfigurationProperties("spring.vivid.rest.polling")
data class VividPollingProperties(
    val enabled: Boolean = true,
    val interval: Duration = Duration.ofMinutes(60),
    val pollType: PollType = PollType.REFRESH,
) {
    enum class PollType {
        /**
         * The feature stream will poll all features from Vivid, including unused features.
         *
         * This might increase response data from Vivids backend and increase local memory consumption
         */
        ALL,

        /**
         * The feature stream will only poll features that are currently used.
         *
         * This uses less local memory and bandwidth but increases the requests from client to Vivid backend.
         */
        REFRESH,
    }
}
