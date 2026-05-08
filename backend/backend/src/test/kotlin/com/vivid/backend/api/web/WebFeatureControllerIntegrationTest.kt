package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.FeatureCreateRequest
import com.vivid.backend.api.web.dto.FeatureDto
import com.vivid.backend.domain.repository.FeatureRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.json.JsonMapper
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebFeatureControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: JsonMapper

    @Autowired
    private lateinit var featureRepository: FeatureRepository

    @BeforeEach
    fun setup() {
        featureRepository.deleteAll()
    }

    @Test
    @WithMockUser
    fun `should create and retrieve feature`() {
        val createRequest = FeatureCreateRequest(
            name = "integration-test-feature",
            description = "integration test desc",
            tags = listOf("test-tag")
        )

        val createResult = mockMvc.perform(
            post("/api/web/features")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        ).andExpect(status().isCreated)
            .andReturn()

        val createdFeature = objectMapper.readValue(createResult.response.contentAsString, FeatureDto::class.java)
        assertEquals(createRequest.name, createdFeature.name)
        assertEquals(createRequest.description, createdFeature.description)

        mockMvc.perform(
            get("/api/web/features/${createdFeature.id}")
                .with(jwt())
        ).andExpect(status().isOk)
            .andExpect { result ->
                val retrievedFeature = objectMapper.readValue(result.response.contentAsString, FeatureDto::class.java)
                assertEquals(createdFeature.id, retrievedFeature.id)
                assertEquals("integration-test-feature", retrievedFeature.name)
            }
    }
}
