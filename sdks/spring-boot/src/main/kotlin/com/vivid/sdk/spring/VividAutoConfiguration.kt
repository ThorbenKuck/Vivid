package com.vivid.sdk.spring

import com.vivid.sdk.*
import com.vivid.sdk.caches.FetchingFeatureCache
import com.vivid.sdk.caches.SimpleFeatureCache
import com.vivid.sdk.spring.condition.ConditionalOnVivid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationPropertiesSource
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource

private val logger = LoggerFactory.getLogger(VividAutoConfiguration::class.java)

@AutoConfiguration
@EnableConfigurationProperties(VividProperties::class)
@PropertySource("classpath:default.vivid.properties")
@ConditionalOnVivid
class VividAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Features::class)
    fun features(
        featureApi: ObjectProvider<FeatureApi>,
        featureCache: ObjectProvider<FeatureCache>,
        vividProperties: VividProperties,
    ): ModifiableFeatures {
        logger.info("Initializing Vivid SDK with streams: ${vividProperties.streams}")

        val cacheInstance = featureCache.ifUnique
        if (cacheInstance != null) {
            logger.debug("Using feature cache from application context for Features instance: {}", cacheInstance)
            return CacheBasedFeatures(cacheInstance)
        }

        val featureApiInstance = featureApi.ifUnique
        if (featureApiInstance != null) {
            logger.debug(
                "Configuring custom FeatureCache implementation for FeatureApi instance: {}",
                featureApiInstance
            )
            return CacheBasedFeatures(FetchingFeatureCache(featureApiInstance))
        }

        logger.warn("No FeatureApi or FeatureCache bean found in application context, using default cache implementation. This may impact feature availability.")
        logger.info("Consider adding a FeatureApi bean or FeatureCache bean to your application context to improve usability.")
        return CacheBasedFeatures(SimpleFeatureCache())
    }

    @Bean
    @ConditionalOnMissingBean(FeatureStreamAggregator::class)
    fun featureStreamAggregator(
        features: ModifiableFeatures,
        featureStreams: ObjectProvider<FeatureStream>,
        vividProperties: VividProperties,
    ): FeatureStreamAggregator {
        return FeatureStreamAggregator(
            features = features,
            featureStreams = featureStreams,
            vividProperties.autostartStreams
        )
    }
}
