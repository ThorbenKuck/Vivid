package com.vivid.backend.service

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.VividClientEntity
import com.vivid.backend.domain.repository.EnvironmentRepository
import com.vivid.backend.domain.repository.VividClientRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*

class VividClientServiceTest {

    private val vividClientRepository = mock(VividClientRepository::class.java)
    private val environmentRepository = mock(EnvironmentRepository::class.java)
    private val service = VividClientService(vividClientRepository, environmentRepository, repositoryTagsProvider)

    @Test
    fun `should register new client`() {
        val externalId = "client-1"
        val envId = UUID.randomUUID()
        val environment = EnvironmentEntity(id = envId, name = "Prod", key = "prod")
        
        `when`(vividClientRepository.findByExternalIdAndEnvironmentId(externalId, envId)).thenReturn(null)
        `when`(environmentRepository.findById(envId)).thenReturn(Optional.of(environment))
        `when`(vividClientRepository.save(any(VividClientEntity::class.java))).thenAnswer { it.arguments[0] }

        val result = service.registerHeartbeat(
            externalId = externalId,
            clientName = "Service A",
            environmentId = envId,
            technologies = setOf("SSE"),
            clientVersion = "1.0.0"
        )

        assertNotNull(result)
        assertEquals(externalId, result.externalId)
        assertEquals("Service A", result.clientName)
        assertEquals(environment, result.environment)
        assertEquals("1.0.0", result.clientVersion)
        assertTrue(result.technologies.contains("SSE"))
    }

    @Test
    fun `should update existing client`() {
        val externalId = "client-1"
        val envId = UUID.randomUUID()
        val environment = EnvironmentEntity(id = envId, name = "Prod", key = "prod")
        val existing = VividClientEntity(
            externalId = externalId,
            clientName = "Old Name",
            environment = environment,
            clientVersion = "0.9.0"
        )

        `when`(vividClientRepository.findByExternalIdAndEnvironmentId(externalId, envId)).thenReturn(existing)
        `when`(vividClientRepository.save(any(VividClientEntity::class.java))).thenAnswer { it.arguments[0] }

        val result = service.registerHeartbeat(
            externalId = externalId,
            clientName = "New Name",
            environmentId = envId,
            technologies = setOf("SSE", "POLLING"),
            clientVersion = "1.0.0"
        )

        assertEquals("New Name", result.clientName)
        assertEquals("1.0.0", result.clientVersion)
        assertEquals(2, result.technologies.size)
        verify(vividClientRepository, times(1)).save(existing)
    }
}
