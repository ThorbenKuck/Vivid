package com.vivid.sdk.spring

import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient

data class HeaderNamesProperties(
    val applicationName: String = "X-Vivid-Application-Name",
    val clientToken: String = "X-Vivid-Client-Token",
    val environment: String = "X-Vivid-Environment",
) {
    fun applyTo(headers: HttpHeaders, vividProperties: VividProperties) {
        headers.add(applicationName, vividProperties.applicationName)
        headers.add(environment, vividProperties.environment)
        vividProperties.clientToken?.let { token -> headers.add(clientToken, token) }
    }

    fun applyTo(webClientBuild: WebClient.RequestHeadersSpec<*>, vividProperties: VividProperties) {
        webClientBuild.header(environment, vividProperties.environment)
        vividProperties.applicationName?.let { webClientBuild.header(applicationName, it) }
        vividProperties.clientToken?.let { webClientBuild.header(clientToken, it) }
    }
}
