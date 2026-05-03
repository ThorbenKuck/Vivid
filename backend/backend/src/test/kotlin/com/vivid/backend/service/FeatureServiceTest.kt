package com.vivid.backend.service

import com.vivid.backend.domain.entity.FeatureEntity
import com.vivid.backend.domain.repository.FeatureRepository
import com.vivid.backend.api.web.dto.FeatureCreateRequest
import com.vivid.backend.api.web.dto.FeatureUpdateRequest
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.*

@ExtendWith(MockitoExtension::class)
class FeatureServiceTest {

    @Mock
    private lateinit var featureRepository: FeatureRepository

    @Mock
    private lateinit var environmentStream: EnvironmentStream

    @Mock
    private lateinit var environmentService: EnvironmentService

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var featureService: FeatureService

    @Test
    fun `should create feature successfully`() {
        val request = FeatureCreateRequest(name = "test-feature", description = "test desc")
        val feature = FeatureEntity(name = request.name, description = request.description, runningNumber = 0, key = "test-feature")
        
        whenever(featureRepository.getNextRunningNumber()).thenReturn(42L)
        whenever(featureRepository.save(any<FeatureEntity>())).thenReturn(feature)
        
        val result = featureService.createFeature(request)
        
        assertNotNull(result)
        assertEquals(request.name, result.name)
    }

    @Test
    fun `should get feature by id`() {
        val id = UUID.randomUUID()
        val feature = FeatureEntity(id = id, name = "test-feature", key = "test-feature", runningNumber = 0)
        
        whenever(featureRepository.findById(id)).thenReturn(Optional.of(feature))
        
        val result = featureService.getFeatureById(id)
        
        assertEquals(id, result.id)
        assertEquals("test-feature", result.name)
    }

    @Test
    fun `should throw exception when feature not found`() {
        val id = UUID.randomUUID()
        whenever(featureRepository.findById(id)).thenReturn(Optional.empty())
        
        assertThrows(ResourceNotFoundException::class.java) {
            featureService.getFeatureById(id)
        }
    }

    @Test
    fun `should update feature`() {
        val id = UUID.randomUUID()
        val existingFeature = FeatureEntity(id = id, name = "old-name", key = "old-name", runningNumber = 0)
        val updateRequest = FeatureUpdateRequest(name = "new-name", description = null, tags = null)
        
        whenever(featureRepository.findById(id)).thenReturn(Optional.of(existingFeature))
        whenever(featureRepository.save(any<FeatureEntity>())).thenAnswer { it.arguments[0] }
        
        val result = featureService.updateFeature(id, updateRequest)
        
        assertEquals("new-name", result.name)
    }
}
