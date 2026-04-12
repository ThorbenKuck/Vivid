package com.vivid.sdk.spring

import com.vivid.sdk.FeatureStream
import com.vivid.sdk.ModifiableFeatures
import com.vivid.sdk.Subscription
import com.vivid.sdk.api.Feature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.ObjectProvider

private val logger = LoggerFactory.getLogger(FeatureStreamAggregator::class.java)

/**
 * Aggregator for multiple [FeatureStream] instances.
 *
 * This class is responsible for starting and stopping all enabled [FeatureStream] instances and updating the local [com.vivid.sdk.Features] state.
 */
class FeatureStreamAggregator(
    private val features: ModifiableFeatures,
    private val featureStreams: ObjectProvider<FeatureStream>,
    private val autostart: Boolean,
) : InitializingBean, DisposableBean {

    private var running = false
    private val subscriptions = mutableListOf<Subscription>()

    override fun afterPropertiesSet() {
        if (autostart) {
            start()
        }
    }

    fun start() {
        synchronized(this) {
            if (running) {
                error("FeatureStreamAggregator is already running")
            }
            val subscriptions = featureStreams.mapNotNull { stream ->
                try {
                    stream.subscribe(FeatureStreamCallback())
                } catch (e: Throwable) {
                    logger.error("Error subscribing to feature stream", e)
                    null
                }
            }
            logger.info("Initializing {} feature streams", subscriptions.size)

            this.subscriptions.addAll(subscriptions)
            running = true
        }
    }

    override fun destroy() {
        synchronized(this) {
            if (!running) {
                error("FeatureStreamAggregator is not running")
            }
            subscriptions.forEach { subscription ->
                try {
                    subscription.cancel()
                } catch (e: Throwable) {
                    logger.error("Error cancelling subscription", e)
                }
            }
            subscriptions.clear()
            running = false
        }
    }

    inner class FeatureStreamCallback : FeatureStream.Callback {
        override fun onNext(feature: Feature) {
            features.set(feature)
        }

        override fun setAll(featureValues: List<Feature>) {
            features.setAll(featureValues)
        }
    }
}
