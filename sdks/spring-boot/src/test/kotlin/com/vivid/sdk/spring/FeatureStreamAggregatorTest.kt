package com.vivid.sdk.spring

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.vivid.sdk.api.Feature
import com.vivid.sdk.api.metadata.StringMetadataValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.wiremock.spring.EnableWireMock
import java.time.Instant
import java.util.UUID.randomUUID

@SpringBootTest(
    useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE,
)
@TestPropertySource(
    properties = [
        "spring.vivid.rest.enabled=true",
        "spring.vivid.streams=rest",
        "spring.vivid.rest.baseUrl=\${wiremock.server.baseUrl}",
    ]
)
@EnableWireMock
class FeatureStreamAggregatorTest @Autowired constructor(
    val featureStreamAggregator: FeatureStreamAggregator,
    val jsonMapper: tools.jackson.databind.json.JsonMapper,
) {

    @Test
    fun `starting the aggregator should start the rest stream`() {
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
        featureStreamAggregator.start()

        // assert
        Thread.sleep(1000)
        verify(getRequestedFor(urlEqualTo("/api/client/features/test")))
    }
}