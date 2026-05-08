package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.DistributionProviderDto
import com.vivid.backend.api.web.dto.SettingsDto
import com.vivid.backend.api.web.dto.toDto
import com.vivid.backend.service.FeatureDistributionProvider
import com.vivid.backend.service.SettingsService
import org.springframework.context.ApplicationContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/web/settings")
class WebSettingsController(
    private val applicationContext: ApplicationContext,
    private val settingsService: SettingsService,
) {

    @GetMapping("/distribution")
    @PreAuthorize("@permissionService.hasPermission('settings', 'read')")
    fun getDistributionProviders(): List<DistributionProviderDto> {
        val providers = applicationContext.getBeansOfType(FeatureDistributionProvider::class.java)
        return providers.map { (name, provider) ->
            DistributionProviderDto(
                name = name,
                type = provider.javaClass.simpleName,
                status = "ACTIVE"
            )
        }
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('settings', 'read')")
    fun getSettings(): SettingsDto {
        return settingsService.getSettings().toDto()
    }

    @PutMapping
    @PreAuthorize("@permissionService.hasPermission('settings', 'write')")
    fun setSettings(@RequestBody settingsDto: SettingsDto): SettingsDto {
        return settingsService.updateSettings {
            it.requireClientTokens = settingsDto.requireClientTokens
            it.allowDynamicClientRegistration = settingsDto.allowDynamicClientRegistration
            it.onlineThreshold = settingsDto.onlineThreshold
        }.toDto()
    }
}
