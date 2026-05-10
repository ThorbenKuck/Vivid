package com.vivid.sdk.spring

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.vivid.clients.api.metadata.StringMetadataValue
import com.vivid.sdk.newFeature
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.wiremock.spring.EnableWireMock
import tools.jackson.databind.json.JsonMapper
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
    val jsonMapper: JsonMapper,
) {

    @Test
    @Disabled
    fun `starting the aggregator should start the rest stream`() {
        // arrange
        val id = randomUUID().toString().replace("-", "")
        val feature = newFeature {
            id(id)
            name("test-feature")
            key("test-feature")
            enabled(true)
            flag("test-flag", true)
            metadata("test-metadata", StringMetadataValue("test-value"))
            build()
        }
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