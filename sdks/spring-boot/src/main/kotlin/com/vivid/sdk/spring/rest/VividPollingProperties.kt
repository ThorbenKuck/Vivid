package com.vivid.sdk.spring.rest

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("spring.vivid.rest.polling")
data class VividPollingProperties(
    val enabled: Boolean = true,
    val interval: Duration = Duration.ofMinutes(60),
)
