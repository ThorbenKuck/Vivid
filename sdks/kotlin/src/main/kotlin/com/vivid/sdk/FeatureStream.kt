package com.vivid.sdk

import com.vivid.sdk.api.Feature

/**
 * A feature stream is a source of feature updates.
 *
 * FeatureStreams are used to receive push updates from your vivid instance about updated Features.
 *
 * Each feature stream is responsible for managing the connection to the vivid instance and handling the subscription to the feature updates.
 * A feature stream is a long-running process that should be managed by the application.
 *
 * One example of a feature stream could be a KafkaFeatureStream.
 * This feature stream would connect to a Kafka topic and subscribe to updates.
 * Whenever a new feature update is published to the Kafka topic, the KafkaFeatureStream will call the callback with the updated feature.
 * This way, the application can react to feature updates immediately, invalidating the local caches and be up to date.
 *
 * Implementations of the api are responsible for handling the connections and providing technologies.
 */
interface FeatureStream {

    /**
     * Subscribe to updates from the feature stream.
     *
     * Whenever a new update is received, the [Callback] will be notified.
     *
     * @param callback the callback to notify when an update is received
     * @return a [Subscription] instance that can be used to unsubscribe from the stream
     */
    fun subscribe(callback: Callback): Subscription

    /**
     * Callback for receiving feature updates from a [FeatureStream].
     */
    interface Callback {
        /**
         * Called whenever a single feature has been updated.
         *
         * @param feature the updated feature state
         */
        fun onNext(feature: Feature)

        /**
         * Called when all features have been updated at once.
         *
         * This can happen during initial synchronization or when a bulk update is received.
         *
         * @param featureValues a list of all current feature states
         */
        fun setAll(featureValues: List<Feature>)
    }
}
