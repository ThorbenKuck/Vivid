package com.vivid.backend.service

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.VividClientEntity
import com.vivid.backend.domain.entity.internal.SettingsEntity
import com.vivid.backend.domain.repository.VividClientRepository
import com.vivid.backend.domain.support.ApplicationIdentifier
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*

class VividClientServiceTest {

    private val vividClientRepository = mock(VividClientRepository::class.java)
    private val settingsService = mock(SettingsService::class.java)
    private val featureUsageService = mock(FeatureUsageService::class.java)
    private lateinit var service: VividClientService

    @BeforeEach
    fun setup() {
        service = VividClientService(vividClientRepository, settingsService, featureUsageService)
        `when`(settingsService.getSettings()).thenReturn(SettingsEntity())
    }

    @Test
    fun `should seen client`() {
        val envId = UUID.randomUUID()
        val environment = EnvironmentEntity(id = envId, name = "Prod", key = "prod")
        val appId = ApplicationIdentifier("ServiceA", environment, "token")
        
        val existing = VividClientEntity(
            clientName = "ServiceA",
            environment = environment,
            clientToken = "token"
        )
        
        `when`(vividClientRepository.findByClientTokenAndEnvironment("token", environment)).thenReturn(existing)
        `when`(vividClientRepository.save(any(VividClientEntity::class.java))).thenAnswer { it.arguments[0] }

        val result = service.seen(appId)

        assertNotNull(result)
        assertEquals("ServiceA", result.clientName)
        assertNotNull(result.lastSeen)
    }
}
