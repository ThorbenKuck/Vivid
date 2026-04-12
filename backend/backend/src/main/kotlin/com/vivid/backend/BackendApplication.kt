package com.vivid.backend

import com.vivid.backend.api.config.ApiSecurityProperties
import com.vivid.backend.service.PermissionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(PermissionProperties::class, ApiSecurityProperties::class)
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}
