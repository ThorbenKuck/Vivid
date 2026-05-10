package com.vivid.backend.clients.streams

import com.vivid.backend.clients.streams.dto.FeatureChangedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.concurrent.ExecutorService

private val logger = LoggerFactory.getLogger(ClientStreams::class.java)

open class ClientStreams(
    private val streams: List<ClientStream>,
    private val executorService: ExecutorService,
): InitializingBean, DisposableBean {
    override fun afterPropertiesSet() {
        streams.forEach {
            logger.info(" - Starting stream: {}", it)
            executorService.execute {
                it.start()
            }
        }
    }

    override fun destroy() {
        logger.info("Shutting down client streams")
        streams.forEach {
            logger.info(" - Stopping stream: {}", it)
            it.shutdown()
            logger.info(" - Stream stopped: {}", it)
        }
        executorService.shutdown()
    }

    open fun push(featureChangedEvent: FeatureChangedEvent) {
        streams.forEach {
            logger.debug("Pushing event to stream: {}", it)
            it.push(featureChangedEvent)
        }
    }

    @TransactionalEventListener(FeatureChangedEvent::class, phase = TransactionPhase.AFTER_COMMIT)
    @Async
    open fun receiveClientChangedEvent(event: FeatureChangedEvent) {
        logger.debug("Feature Change Event detected: {}", event)
        push(event)
    }
}
