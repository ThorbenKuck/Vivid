package com.vivid.sdk.spring.sse

import com.vivid.sdk.spring.HeaderNamesProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.vivid.sse")
data class VividSseProperties(
    val baseUrl: String,
    val enabled: Boolean = true,
    val headerNames: HeaderNamesProperties = HeaderNamesProperties(),
)
