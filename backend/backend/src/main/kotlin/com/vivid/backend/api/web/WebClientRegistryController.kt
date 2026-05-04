package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.ClientRegistryDto
import com.vivid.backend.api.web.dto.toDto
import com.vivid.backend.service.SettingsService
import com.vivid.backend.service.VividClientService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@RestController
@RequestMapping("/api/web/clients")
class WebClientRegistryController(
    private val vividClientService: VividClientService,
    private val settingsService: SettingsService,
) {
    @GetMapping
    fun getAllClients(): List<ClientRegistryDto> {
        val onlineThreshold = settingsService.getSettings().onlineThreshold
        return vividClientService.getAllClients().map { it.toDto(onlineThreshold) }
    }
}
