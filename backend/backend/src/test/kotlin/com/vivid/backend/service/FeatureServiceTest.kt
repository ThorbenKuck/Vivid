package com.vivid.backend.service

import com.vivid.backend.domain.entity.Feature
import com.vivid.backend.domain.repository.FeatureEnvironmentRepository
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
    private lateinit var featureEnvironmentRepository: FeatureEnvironmentRepository

    @Mock
    private lateinit var teamRepository: com.vivid.backend.domain.repository.TeamRepository

    @Mock
    private lateinit var environmentService: EnvironmentService

    @Mock
    private lateinit var departmentService: DepartmentService

    @InjectMocks
    private lateinit var featureService: FeatureService

    private val departmentId = UUID.randomUUID()
    private val department = com.vivid.backend.domain.entity.Department(id = departmentId, name = "General")

    @Test
    fun `should create feature successfully`() {
        val request = FeatureCreateRequest(name = "test-feature", description = "test desc")
        val feature = Feature(name = request.name, description = request.description, department = department)
        
        whenever(departmentService.findById(departmentId)).thenReturn(department)
        whenever(featureRepository.getNextRunningNumber()).thenReturn(42L)
        whenever(featureRepository.save(any<Feature>())).thenReturn(feature)
        
        val result = featureService.createFeature(departmentId, request)
        
        assertNotNull(result)
        assertEquals(request.name, result.name)
        assertEquals(departmentId, result.department.id)
    }

    @Test
    fun `should get feature by id`() {
        val id = UUID.randomUUID()
        val feature = Feature(id = id, name = "test-feature", department = department)
        
        whenever(featureRepository.findById(id)).thenReturn(Optional.of(feature))
        
        val result = featureService.getFeatureById(id, departmentId, null)
        
        assertEquals(id, result.first.id)
        assertEquals("test-feature", result.first.name)
    }

    @Test
    fun `should throw exception when feature not found`() {
        val id = UUID.randomUUID()
        whenever(featureRepository.findById(id)).thenReturn(Optional.empty())
        
        assertThrows(ResourceNotFoundException::class.java) {
            featureService.getFeatureById(id, departmentId, null)
        }
    }

    @Test
    fun `should update feature`() {
        val id = UUID.randomUUID()
        val existingFeature = Feature(id = id, name = "old-name", department = department)
        val updateRequest = FeatureUpdateRequest(name = "new-name", description = null, tags = null)
        
        whenever(featureRepository.findById(id)).thenReturn(Optional.of(existingFeature))
        whenever(featureRepository.save(any<Feature>())).thenAnswer { it.arguments[0] }
        
        val result = featureService.updateFeature(id, departmentId, updateRequest)
        
        assertEquals("new-name", result.first.name)
    }
}
