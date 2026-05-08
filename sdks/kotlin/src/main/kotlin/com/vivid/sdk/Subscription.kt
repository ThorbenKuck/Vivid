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

    companion object {
        operator fun invoke(layers: Collection<Subscription>) = CompositeSubscription(layers)
    }

    class CompositeSubscription(private val layers: Collection<Subscription>) : Subscription {
        override fun cancel() {
            layers.forEach { it.cancel() }
            if (layers is MutableCollection<*>) {
                layers.clear()
            }
        }
    }

    object Empty : Subscription {
        override fun cancel() {}
    }
}
