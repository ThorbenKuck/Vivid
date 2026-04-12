package com.vivid.sdk.spring.sse

import com.vivid.sdk.FeatureStream
import com.vivid.sdk.Subscription
import com.vivid.sdk.api.Feature
import com.vivid.sdk.spring.VividProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.Disposable
import reactor.core.publisher.Flux

/**
 * [FeatureStream] implementation that uses Server-Sent Events (SSE) to receive feature updates.
 */
class SseFeatureStream(
    private val webClient: WebClient,
    private val sseProperties: VividSseProperties,
    private val vividProperties: VividProperties
) : FeatureStream {

    companion object {
        private val SSE_TYPE = object : ParameterizedTypeReference<ServerSentEvent<Feature>>() {}
    }

    override fun subscribe(callback: FeatureStream.Callback): Subscription {
        val webClientBuild = webClient.get().uri {
            it.pathSegment("api", "client", "features", "{environment}", "stream")
                .build(vividProperties.environment)
        }

        sseProperties.apiKey?.let { webClientBuild.header(sseProperties.apiTokenHeaderName, it) }
        vividProperties.applicationId?.let { webClientBuild.header(sseProperties.applicationIdHeaderName, it) }

        val eventStream: Flux<ServerSentEvent<Feature>> = webClientBuild.retrieve()
            .bodyToFlux(SSE_TYPE)

        val disposable = eventStream.subscribe { event ->
            event.data()?.let { callback.onNext(it) }
        }

        return SseSubscription(disposable)
    }

    private class SseSubscription(
        private val disposable: Disposable
    ) : Subscription {
        override fun cancel() {
            synchronized(disposable) {
                if (disposable.isDisposed.not()) {
                    disposable.dispose()
                }
            }
        }
    }
}