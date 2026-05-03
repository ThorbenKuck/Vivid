package com.vivid.backend

import com.vivid.backend.api.config.ApiSecurityProperties
import com.vivid.backend.service.PermissionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import java.util.UUID

@SpringBootApplication
@EnableConfigurationProperties(PermissionProperties::class, ApiSecurityProperties::class)
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