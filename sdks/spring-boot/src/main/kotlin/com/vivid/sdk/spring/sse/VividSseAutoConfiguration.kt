package com.vivid.sdk.spring.sse

import com.vivid.sdk.spring.VividProperties
import com.vivid.sdk.spring.condition.ConditionalOnFeatureStream
import com.vivid.sdk.spring.condition.ConditionalOnVivid
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@ConditionalOnClass(WebClient::class)
@EnableConfigurationProperties(VividSseProperties::class)
@ConditionalOnFeatureStream("sse")
@ConditionalOnVivid("sse")
class VividSseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SseFeatureStream::class)
    fun sseFeatureStream(
        sseProperties: VividSseProperties,
        properties: VividProperties
    ): SseFeatureStream {
        val client = WebClient.builder()
            .baseUrl(sseProperties.baseUrl)
            .build()

        return SseFeatureStream(client, sseProperties, properties)
    }
}
