package com.vivid.sdk.spring.kafka

import com.vivid.sdk.FeatureCache
import com.vivid.sdk.FeatureStream
import com.vivid.sdk.Subscription
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerEndpointRegistry

/**
 * [FeatureStream] implementation that uses Kafka to receive feature updates.
 */
class KafkaFeatureStream(
    private val registry: KafkaListenerEndpointRegistry,
    private val properties: VividKafkaProperties,
    private val factory: KafkaListenerContainerFactory<*>,
) : FeatureStream {

    override fun subscribe(callback: FeatureStream.Callback): Subscription {
        var batch = properties.batching.enabled ?: false
        if (factory is ConcurrentKafkaListenerContainerFactory<*, *>) {
            factory.isBatchListener?.let { batch = it }
        }

        val endpoint = VividKafkaListenerEndpoint(properties, batch)
        registry.registerListenerContainer(endpoint, factory, true)
        return KafkaSubscription(endpoint)
    }

    inner class KafkaSubscription(
        private val endpoint: VividKafkaListenerEndpoint,
    ) : Subscription {
        override fun cancel() {
            registry.getListenerContainer(endpoint.id)?.stop()
            registry.unregisterListenerContainer(endpoint.id)
        }
    }
}
