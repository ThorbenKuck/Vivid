package com.vivid.sdk;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A handle to a subscription.
 * <p>
 * A subscription can be cancelled to stop receiving updates.
 */
public interface Subscription {
    Empty EMPTY = new Empty();

    static Subscription composite(Subscription... layers) {
        return composite(Arrays.asList(layers));
    }

    static Subscription composite(List<Subscription> layers) {
        if (layers.isEmpty()) {
            return EMPTY;
        }
        return new CompositeSubscription(layers);
    }

    /**
     * Cancel the subscription.
     * <p>
     * Once cancelled, no more updates will be received.
     */
    void cancel();

    class CompositeSubscription implements Subscription {
        private final Collection<Subscription> layers;

        public CompositeSubscription(Collection<Subscription> layers) {
            this.layers = layers;
        }

        @Override
        public void cancel() {
            layers.forEach(Subscription::cancel);
        }
    }

    class Empty implements Subscription {
        @Override
        public void cancel() {
            // NoOp
        }
    }

}
