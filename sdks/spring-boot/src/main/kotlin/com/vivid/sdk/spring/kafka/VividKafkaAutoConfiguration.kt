package com.vivid.sdk.spring.kafka

import com.vivid.sdk.spring.condition.ConditionalOnFeatureStream
import com.vivid.sdk.spring.condition.ConditionalOnVivid
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

/**
 * AutoConfiguration for the Kafka-based feature streaming.
 *
 * This configuration provides the [KafkaFeatureStream] bean if the "kafka" stream is enabled.
 */
@AutoConfiguration
@EnableConfigurationProperties(VividKafkaProperties::class)
@ConditionalOnClass(KafkaListenerEndpointRegistry::class)
@PropertySource("classpath:default.vivid-kafka.properties")
@ConditionalOnVivid("kafka")
@ConditionalOnProperty("spring.vivid.kafka.topics")
class VividKafkaAutoConfiguration {

    @Bean
    @ConditionalOnBean(KafkaListenerEndpointRegistry::class)
    @ConditionalOnFeatureStream("kafka")
    fun kafkaFeatureStream(
        factory: KafkaListenerEndpointRegistry,
        properties: VividKafkaProperties,
        kafkaListenerContainerFactory: ObjectProvider<KafkaListenerContainerFactory<*>>,
        applicationContext: ApplicationContext,
    ): KafkaFeatureStream {
        val containerFactory = when (properties.consumerFactory.takeFrom) {
            VividKafkaProperties.ContainerFactoryContext.BEAN_NAME -> applicationContext.getBean(
                properties.consumerFactory.beanName ?: error("Missing bean name for consumer factory"),
                KafkaListenerContainerFactory::class.java
            )

            VividKafkaProperties.ContainerFactoryContext.APPLICATION_CONTEXT -> kafkaListenerContainerFactory.getIfUnique() ?: error("Could not find a unique KafkaListenerContainerFactory bean in the application context.")
            VividKafkaProperties.ContainerFactoryContext.VIVID -> customKafkaListenerContainerFactory(properties)
            else -> error("Unknown consumer factory takeFrom property: ${properties.consumerFactory.takeFrom}")
        }

        return KafkaFeatureStream(
            factory,
            properties,
            containerFactory,
        )
    }

    private fun customKafkaListenerContainerFactory(properties: VividKafkaProperties): ConcurrentKafkaListenerContainerFactory<ByteArray, ByteArray> {
        val factory = ConcurrentKafkaListenerContainerFactory<ByteArray, ByteArray>()
        factory.setConsumerFactory(properties.defaultKafkaConsumerFactory())
        factory.setBatchListener(true)

        return factory
    }

    private fun VividKafkaProperties.defaultKafkaConsumerFactory(): DefaultKafkaConsumerFactory<ByteArray, ByteArray> {
        val properties: MutableMap<String, Any> = mutableMapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
        )

        properties[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = batching.maxPollRecords
        properties[ConsumerConfig.FETCH_MIN_BYTES_CONFIG] = batching.minFetchBytes
        properties[ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG] = batching.maxWaitTime.toMillis()

        return DefaultKafkaConsumerFactory(properties, ByteArrayDeserializer(), ByteArrayDeserializer())
    }
}
