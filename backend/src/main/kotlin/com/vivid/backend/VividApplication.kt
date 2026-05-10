package com.vivid.backend

import com.vivid.backend.api.config.ApiSecurityProperties
import com.vivid.backend.clients.api.ClientProperties
import com.vivid.backend.service.PermissionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import java.util.*

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(
    PermissionProperties::class,
    ApiSecurityProperties::class,
    ClientProperties::class,
    ApplicationProperties::class,
)
class VividApplication

fun main(args: Array<String>) {
    runApplication<VividApplication>(*args)
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
