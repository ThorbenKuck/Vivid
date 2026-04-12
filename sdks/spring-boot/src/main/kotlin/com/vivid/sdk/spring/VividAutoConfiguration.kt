package com.vivid.sdk.spring

import com.vivid.sdk.*
import com.vivid.sdk.caches.FetchingFeatureCache
import com.vivid.sdk.caches.SimpleFeatureCache
import com.vivid.sdk.spring.condition.ConditionalOnVivid
import com.vivid.sdk.spring.qualifier.VividAutowireCandidateResolver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationPropertiesSource
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource

private val logger = LoggerFactory.getLogger(VividAutoConfiguration::class.java)

/**
 * Main AutoConfiguration for the Vivid SDK in Spring Boot.
 *
 * This configuration provides the [Features] and [FeatureStreamAggregator] beans.
 */
@AutoConfiguration
@EnableConfigurationProperties(VividProperties::class)
@PropertySource("classpath:default.vivid.properties")
@ConditionalOnVivid
class VividAutoConfiguration {

    @Bean
    fun vividBeanFactoryPostProcessor(
        featureFactory: Features
    ): BeanFactoryPostProcessor {
        return BeanFactoryPostProcessor { beanFactory ->
            val defaultBeanFactory = beanFactory as DefaultListableBeanFactory
            defaultBeanFactory.autowireCandidateResolver =
                VividAutowireCandidateResolver(featureFactory)
        }
    }

    @Bean
    @ConditionalOnMissingBean(Features::class)
    fun features(
        featureApi: ObjectProvider<FeatureApi>,
        featureCache: FeatureCache,
    ): ModifiableFeatures {
        logger.info("Consider adding a FeatureApi bean or FeatureCache bean to your application context to improve usability.")
        return CacheBasedFeatures(featureCache)
    }

    @Bean
    @ConditionalOnMissingBean(FeatureCache::class)
    fun featureCache(featureApi: ObjectProvider<FeatureApi>): FeatureCache {
        val api = featureApi.ifUnique
        if (api == null) {
            logger.warn("No FeatureApi or FeatureCache bean found in application context, using default cache implementation. This may impact feature availability.")
            return SimpleFeatureCache()
        }
        return FetchingFeatureCache(api)
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
            vividProperties.autostartStreams,
        )
    }
}
