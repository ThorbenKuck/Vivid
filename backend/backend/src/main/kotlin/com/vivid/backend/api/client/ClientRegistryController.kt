package com.vivid.backend.api.client

import com.vivid.backend.api.client.dto.HeartbeatRequest
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.VividClientService
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
    fun heartbeat(@RequestBody request: HeartbeatRequest) {
        vividClientService.registerHeartbeat(request.toEntity {
            environmentService.findEnvironment(request.environment)
                ?: throw IllegalArgumentException("Environment not found: ${request.environment}")
        })
    }
}
