package com.vivid.backend.clients.api

import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.VividClientService
import com.vivid.clients.api.Heartbeat
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/client")
class ClientRegistryController(
    private val vividClientService: VividClientService,
    private val environmentService: EnvironmentService,
) {

    @PostMapping("/heartbeat")
    fun heartbeat(@RequestBody request: Heartbeat) {
        val environment = environmentService.findEnvironment(request.environment)
            ?: throw IllegalArgumentException("Environment not found: ${request.environment}")

        vividClientService.registerHeartbeat(
            clientName = request.applicationName ?: "<anonymous>",
            environment = environment,
            clientToken = request.clientToken,
            streams = request.streams,
            clientVersion = request.clientVersion
        )
    }
}
