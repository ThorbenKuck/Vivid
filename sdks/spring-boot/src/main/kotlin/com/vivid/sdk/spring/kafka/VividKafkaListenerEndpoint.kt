package com.vivid.sdk.spring.kafka

import com.vivid.sdk.api.Feature
import org.springframework.kafka.config.MethodKafkaListenerEndpoint
import java.util.UUID
import java.util.regex.Pattern

class VividKafkaListenerEndpoint(
    private val properties: VividKafkaProperties,
    private val batch: Boolean = true,
): MethodKafkaListenerEndpoint<String, Feature>() {

    private val id = "vivid-${UUID.randomUUID().toString().replace("-", "")}"

    override fun getId() = id
    override fun getGroupId() = properties.groupId
    override fun getGroup() = properties.group
    override fun getTopics() = properties.topics
    override fun getTopicPartitionsToAssign() = null
    override fun getTopicPattern() = properties.topicPattern?.let { Pattern.compile(it) }
    override fun getClientIdPrefix() = properties.clientIdPrefix
    override fun getConcurrency() = properties.concurrency
    override fun getAutoStartup() = properties.autoStartup

    override fun isSplitIterables(): Boolean {
        return true
    }

    override fun getConsumerProperties() = properties.buildConsumerProperties()
    override fun getBatchListener(): Boolean? {
        return batch
    }
}