package com.vivid.sdk.spring.rest

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.vivid.sdk.Features
import com.vivid.sdk.api.Feature
import com.vivid.sdk.api.metadata.StringMetadataValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.wiremock.spring.EnableWireMock
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.UUID.randomUUID

@SpringBootTest(
    useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE,
)
@TestPropertySource(
    properties = [
        "spring.vivid.rest.enabled=true",
        "spring.vivid.rest.baseUrl=\${wiremock.server.baseUrl}",
    ]
)
@EnableWireMock
class VividRestAutoConfigurationTest @Autowired constructor(
    private val features: Features,
    private val jsonMapper: JsonMapper,
) {
    @Test
    fun `rest feature api bean is created`() {
        // arrange
        val id = randomUUID().toString().replace("-", "")
        val feature = Feature(
            id = id,
            name = "test-feature",
            enabled = true,
            flags = mapOf("test-flag" to true),
            metadata = mapOf("test-metadata" to StringMetadataValue("test-value")),
            timestamp = Instant.now(),
        )
        stubFor(
            get("/api/client/features/test/${feature.name}")
                .willReturn(
                    okJson(jsonMapper.writeValueAsString(feature))
                )
        )

        stubFor(
            get("/api/client/features/test")
                .willReturn(
                    okJson(jsonMapper.writeValueAsString(listOf(feature)))
                )
        )

        // act
        val get = features.get("test-feature")

        // assert
        assert(get.isEnabled() == true)
        verify(getRequestedFor(urlEqualTo("/api/client/features/test/${feature.name}")))
    }

    @Test
    fun `references are evaluated lazily if at all`() {        // arrange
        val id = randomUUID().toString().replace("-", "")
        val feature = Feature(
            id = id,
            name = "test-feature",
            enabled = true,
            flags = mapOf("test-flag" to true),
            metadata = mapOf("test-metadata" to StringMetadataValue("test-value")),
            timestamp = Instant.now(),
        )
        stubFor(
            get("/api/client/features/test/${feature.name}")
                .willReturn(
                    okJson(jsonMapper.writeValueAsString(feature))
                )
        )

        stubFor(
            get("/api/client/features/test")
                .willReturn(
                    okJson(jsonMapper.writeValueAsString(listOf(feature)))
                )
        )

        // act
        features.reference("test-feature")

        // assert
    }
}