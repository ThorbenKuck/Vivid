package com.vivid.backend

import com.vivid.backend.api.client.ClientProperties
import com.vivid.backend.api.config.ApiSecurityProperties
import com.vivid.backend.service.PermissionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import java.util.UUID

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(PermissionProperties::class, ApiSecurityProperties::class, ClientProperties::class)
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}

fun String.asKey(): String {
    return this.lowercase().replace(" ", "-")
}

fun String.toUuidOrNull(): UUID? {
    return try {
        UUID.fromString(this)
    } catch (_: IllegalArgumentException) {
        null
    }
}