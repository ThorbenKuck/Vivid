package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.ClientRegistryDto
import com.vivid.backend.api.web.dto.ClientUpdateRequest
import com.vivid.backend.api.web.dto.toDto
import com.vivid.backend.service.FeatureUsageService
import com.vivid.backend.service.SettingsService
import com.vivid.backend.service.VividClientService
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/web/clients")
class WebClientRegistryController(
    private val vividClientService: VividClientService,
    private val settingsService: SettingsService,
    private val featureUsageService: FeatureUsageService,
) {
    @GetMapping
    fun getAllClients(): List<ClientRegistryDto> {
        val onlineThreshold = settingsService.getSettings().onlineThreshold
        return vividClientService.getAllClients().map { it.toDto(onlineThreshold) }
    }

    @GetMapping("/{id}")
    fun getClient(@PathVariable id: UUID): ClientRegistryDto {
        val onlineThreshold = settingsService.getSettings().onlineThreshold
        val client = vividClientService.findById(id) ?: throw ResourceNotFoundException("Client not found")
        val usage = featureUsageService.getUsageForClient(id)
        return client.toDto(onlineThreshold, usage)
    }

    @DeleteMapping("/{id}")
    fun deleteClient(@PathVariable id: UUID) {
        vividClientService.deleteClient(id)
    }

    @PutMapping("/{id}")
    fun updateClient(@PathVariable id: UUID, @RequestBody request: ClientUpdateRequest): ClientRegistryDto {
        val onlineThreshold = settingsService.getSettings().onlineThreshold
        return vividClientService.updateClient(id, request.clientName, request.clientToken).toDto(onlineThreshold)
    }

    @PostMapping
    fun createClient(@RequestBody request: ClientUpdateRequest): ClientRegistryDto {
        val onlineThreshold = settingsService.getSettings().onlineThreshold
        return vividClientService.createClient(request.clientName, request.clientToken).toDto(onlineThreshold)
    }
}
