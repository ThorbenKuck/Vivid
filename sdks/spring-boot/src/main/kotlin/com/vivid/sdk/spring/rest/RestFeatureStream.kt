package com.vivid.sdk.spring.rest

import com.vivid.sdk.FeatureApi
import com.vivid.sdk.FeatureStream
import com.vivid.sdk.Subscription
import org.springframework.scheduling.TaskScheduler
import java.util.concurrent.ScheduledFuture

class RestFeatureStream(
    private val executor: TaskScheduler,
    private val pollingProperties: VividPollingProperties,
    private val api: FeatureApi,
) : FeatureStream {

    override fun subscribe(callback: FeatureStream.Callback): Subscription {
        val future = executor.scheduleAtFixedRate({
            api.fetchAllFeatures()?.let {
                callback.setAll(it)
            }
        }, pollingProperties.interval)
        return PollingSubscription(future)
    }

    private class PollingSubscription(
        private val future: ScheduledFuture<*>
    ) : Subscription {
        override fun cancel() {
            future.cancel(true)
        }
    }
}