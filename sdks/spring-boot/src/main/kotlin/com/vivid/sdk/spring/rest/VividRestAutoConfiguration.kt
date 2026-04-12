package com.vivid.sdk.spring.rest

import com.vivid.sdk.FeatureApi
import com.vivid.sdk.FeatureCache
import com.vivid.sdk.spring.VividProperties
import com.vivid.sdk.spring.condition.ConditionalOnFeatureStream
import com.vivid.sdk.spring.condition.ConditionalOnVivid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler
import org.springframework.web.client.RestClient

private val logger = LoggerFactory.getLogger(VividRestAutoConfiguration::class.java)

/**
 * AutoConfiguration for the REST-based feature fetching and streaming.
 *
 * This configuration provides the [SpringFeatureApi] and [RestFeatureStream] beans if the "rest" stream is enabled.
 */
@AutoConfiguration
@EnableConfigurationProperties(VividRestProperties::class, VividPollingProperties::class)
@ConditionalOnClass(RestClient::class)
@PropertySource("classpath:default.vivid-rest.properties")
@ConditionalOnVivid("rest")
@ConditionalOnProperty("spring.vivid.rest.base-url")
class VividRestAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SpringFeatureApi::class, FeatureApi::class)
    fun springFeatureApi(
        restProperties: VividRestProperties,
        properties: VividProperties,
    ): SpringFeatureApi {
        logger.debug("Creating SpringFeatureApi with properties: {}", restProperties)
        val restClient = RestClient.builder().baseUrl(restProperties.baseUrl).build()
        return SpringFeatureApi(restClient, restProperties, properties)
    }

    @Bean
    @ConditionalOnFeatureStream("rest")
    @ConditionalOnBean(FeatureApi::class)
    fun restFeatureStream(
        executor: ObjectProvider<TaskScheduler>,
        pollingProperties: VividPollingProperties,
        api: FeatureApi,
        cache: FeatureCache,
    ): RestFeatureStream {
        logger.debug("REST feature stream enabled")
        return RestFeatureStream(
            executor = executor.getIfUnique { SimpleAsyncTaskScheduler() },
            pollingProperties = pollingProperties,
            api = api,
            cache = cache,
        )
    }
}
