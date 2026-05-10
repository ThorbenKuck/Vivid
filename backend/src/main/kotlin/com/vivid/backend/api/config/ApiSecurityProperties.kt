package com.vivid.backend.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("vivid.clients")
class ApiSecurityProperties(
    val token: String? = null,
)
