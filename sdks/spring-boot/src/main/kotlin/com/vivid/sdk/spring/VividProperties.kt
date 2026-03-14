package com.vivid.sdk.spring

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("spring.vivid")
data class VividProperties(
    val environment: String,
    val applicationId: String?,
    val applicationIdHeaderName: String,
    val apiTokenHeaderName: String,
    val streams: Set<String>,
    val enabled: Boolean,
    val autostartStreams: Boolean,
) {
}
