package com.vivid.backend.clients.streams.sse

import com.vivid.backend.domain.support.ApplicationIdentifier
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.VividClientService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@ConditionalOnBean(SseClientStream::class)
@RequestMapping("/api/client/features/{environment}")
@Tag(name = "Client API", description = "Runtime API for SDKs")
class SseClientStreamController(
    private val sseClientStream: SseClientStream,
    private val environmentService: EnvironmentService,
    private val clientService: VividClientService,
) {

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> {
        return ResponseEntity.ok("pong")
    }

    @GetMapping("/stream")
    fun streamFeatures(
        @PathVariable environment: String,
        applicationIdentifier: ApplicationIdentifier?,
    ): ResponseEntity<SseEmitter> {
        applicationIdentifier?.let { clientService.seen(applicationIdentifier) }
        val environment = environmentService.findEnvironment(environment) ?: return ResponseEntity.notFound().build()
        val emitter = sseClientStream.register(environment, applicationIdentifier)
        return ResponseEntity.ok(emitter)
    }
}
