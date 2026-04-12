package com.vivid.sdk.spring.rest

import com.vivid.sdk.FeatureApi
import com.vivid.sdk.FeatureCache
import com.vivid.sdk.FeatureStream
import com.vivid.sdk.Subscription
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import java.util.concurrent.ScheduledFuture

private val logger = LoggerFactory.getLogger(RestFeatureStream::class.java)

/**
 * [FeatureStream] implementation that polls the [FeatureApi] at regular intervals.
 */
class RestFeatureStream(
    private val executor: TaskScheduler,
    private val pollingProperties: VividPollingProperties,
    private val api: FeatureApi,
    private val cache: FeatureCache,
) : FeatureStream {

    override fun subscribe(callback: FeatureStream.Callback): Subscription {
        val future = executor.scheduleAtFixedRate({
            callback.doPoll()
        }, pollingProperties.interval)
        return PollingSubscription(future)
    }

    private fun FeatureStream.Callback.doPoll() {
        if (pollingProperties.pollType == VividPollingProperties.PollType.ALL) {
            logger.debug("Refreshing feature cache with all features from Vivid")
            api.fetchAllFeatures()?.let {
                setAll(it)
            }
        } else if(pollingProperties.pollType == VividPollingProperties.PollType.REFRESH) {
            logger.debug("Refreshing feature cache with known features from Vivid")
            cache.getAll().forEach {
                logger.trace("Refreshing feature: {}", it.name)
                api.fetchFeature(it.name)?.let {
                    onNext(it)
                }
            }
        }
    }

    private class PollingSubscription(
        private val future: ScheduledFuture<*>
    ) : Subscription {
        override fun cancel() {
            future.cancel(true)
        }
    }
}