package com.vivid.sdk.spring.kafka

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import java.util.Properties

/**
 * Configuration properties for the Kafka-based feature streaming.
 *
 * @property groupId the Kafka group ID to use
 * @property bootstrapServers the Kafka bootstrap servers
 * @property topics the set of topics to listen to
 * @property enabled whether Kafka streaming is enabled
 * @property concurrency the number of threads to use for consumption
 * @property clientIdPrefix the client ID prefix
 * @property group the Kafka group
 * @property topicPattern the topic pattern to subscribe to
 * @property autoStartup whether to start the Kafka listener automatically
 * @property consumerProperties additional Kafka consumer properties
 * @property consumerFactory configuration for the Kafka listener container factory
 * @property batching configuration for batch processing
 */
@ConfigurationProperties("spring.vivid.kafka")
data class VividKafkaProperties(
    val enabled: Boolean = true,
    /**
     * The kafka group id that vivid should use.
     *
     * If spring-boot-starter-kafka is in the classpath, this should be the groupId of the application context.
     */
    val groupId: String,
    /**
     * The kafka bootstrap servers that vivid should use.
     *
     * If spring-boot-starter-kafka is in the classpath, this should be bootstrap servers groupId of the application context.
     */
    val bootstrapServers: String,
    /**
     * The topics that vivid should listen to for feature updates.
     */
    val topics: Set<String>,

    val concurrency: Int? = null,
    val clientIdPrefix: String? = null,
    val group: String? = null,
    val topicPattern: String? = null,
    val autoStartup: Boolean = true,
    val consumerProperties: Map<String, String>? = null,
    val consumerFactory: ConsumerFactory = ConsumerFactory(),
    val batching: Batching = Batching(),
) {

    data class Batching(
        /**
         * A boolean flag indicating whether batching is enabled.
         *
         * If true, vivid will assume that the consumer is configured to use batch processing.
         * If the consumer factory is a ConcurrentKafkaListenerContainerFactory, the batch listener flag will be used from there.
         *
         * This flag is required if you have a custom consumer factory that does not inherit from ConcurrentKafkaListenerContainerFactory.
         * In this case, if the flag is not set, vivid will assume that the consumer is not using batch processing.
         *
         * If vivid is creating the ConsumerFactory, then batching is the default.
         */
        val enabled: Boolean? = null,

        /**
         * How many records maximum kafka should fetch in a single poll.
         *
         * @see org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG
         */
        val maxPollRecords: Int = 100,

        /**
         * How many bytes kafka should fetch minimum in a single poll.
         *
         * @see org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MIN_BYTES_CONFIG
         */
        val minFetchBytes: Long = 1024,

        /**
         * The maximum time the consumer should wait for records to arrive.
         *
         * @see org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG
         */
        val maxWaitTime: Duration = Duration.ofSeconds(5),
    )

    data class ConsumerFactory(
        /**
         * The name of the bean that vivid should use to fetch the KafkaListenerContainerFactory.
         *
         * Is only respected if the property `spring.vivid.kafka.consumer-factory.take-from` is set to `BEAN_NAME`.
         */
        val beanName: String? = null,

        /**
         * The context where vivid should look for the KafkaListenerContainerFactory.
         */
        val takeFrom: ContainerFactoryContext? = null,
    )

    enum class ContainerFactoryContext {
        /**
         * Assumes that the developer wants to use a specific bean from the application.
         *
         * If set, the property `spring.vivid.kafka.consumer-factory.bean-name` must be not null.
         */
        BEAN_NAME,

        /**
         * Assumes that the developer wants to use the application context to find the bean.
         *
         * If set, the application context must contain a single KafkaListenerContainerFactory bean.
         */
        APPLICATION_CONTEXT,

        /**
         * Use a custom KafkaListenerContainerFactory that is created by Vivid and configured to use batch processing.
         */
        VIVID,
    }

    fun buildConsumerProperties(): Properties? {
        return consumerProperties?.let { p ->
            Properties().also { properties -> properties.putAll(p) }
        }
    }
}

