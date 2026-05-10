package com.vivid.sdk.spring.sse

import com.vivid.sdk.FeatureStream
import com.vivid.sdk.Subscription
import com.vivid.clients.api.Feature
import com.vivid.sdk.spring.VividProperties
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.util.retry.Retry.backoff
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds

private val logger = LoggerFactory.getLogger(SseFeatureStream::class.java)

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
        logger.debug("Subscribing to SSE feature stream")
        val webClientBuild = webClient.get().uri {
            it.path("/api/client/features/{environment}/stream")
                .build(vividProperties.environment.trim().removeSuffix("/"))
        }

        sseProperties.headerNames.applyTo(webClientBuild, vividProperties)

        val eventStream: Flux<ServerSentEvent<Feature>> = webClientBuild.retrieve()
            .bodyToFlux(SSE_TYPE)
            .timeout(ofMinutes(5))
            .retryWhen(
                backoff(Long.MAX_VALUE, ofSeconds(2))
                    .filter { error ->
                        error !is WebClientResponseException ||
                                (error.statusCode != HttpStatus.FORBIDDEN && error.statusCode != HttpStatus.UNAUTHORIZED && error.statusCode != HttpStatus.NOT_FOUND)
                    }
                    .doBeforeRetry { logger.warn("SSE connection lost. " + it.failure().localizedMessage + ". Retrying...") }
            )

        val disposable = eventStream.subscribe(
            { event ->
                event.data()?.let {
                    logger.debug("Received SSE event with data: {}", event.data())
                    callback.onNext(it)
                }
            }, { error ->
                if (error is WebClientResponseException.Forbidden) {
                    logger.error("Cannot use Sse feature stream: {}", error.responseBodyAsString)
                } else if(error is WebClientResponseException.NotFound) {
                    logger.error("Cannot use Sse feature stream. Vivid is not configured to accept SSE requests")
                } else {
                    logger.error("Error in SSE feature stream: {}", error.message, error)
                }
            }
        )
        logger.debug("Subscription to SSE feature stream established.")

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