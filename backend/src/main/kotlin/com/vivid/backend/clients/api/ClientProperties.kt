package com.vivid.backend.clients.api

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("clients")
class ClientProperties(
    val applicationNameHeader: String = "X-Vivid-Application-Name",
    val clientTokenHeader: String = "X-Vivid-Client-Token",
    val environmentHeader: String = "X-Vivid-Environment",
)
