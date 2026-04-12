package com.vivid.sdk

/**
 * A handle to a subscription.
 *
 * A subscription can be cancelled to stop receiving updates.
 */
interface Subscription {

    /**
     * Cancel the subscription.
     *
     * Once cancelled, no more updates will be received.
     */
    fun cancel()

}
