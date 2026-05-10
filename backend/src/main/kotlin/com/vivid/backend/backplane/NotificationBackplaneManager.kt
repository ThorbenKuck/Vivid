package com.vivid.backend.backplane

import com.vivid.backend.ApplicationProperties
import com.vivid.backend.backplane.dto.Notification
import com.vivid.backend.clients.streams.ClientStreams
import com.vivid.backend.clients.streams.dto.FeatureChangedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.scheduling.annotation.Async
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.concurrent.ExecutorService

private val logger = LoggerFactory.getLogger(NotificationBackplaneManager::class.java)

open class NotificationBackplaneManager(
    private val backplanes: List<NotificationBackplane>,
    private val executor: ExecutorService,
    private val clientStreams: ClientStreams,
    private val applicationProperties: ApplicationProperties,
) : InitializingBean, DisposableBean {
    override fun afterPropertiesSet() {
        backplanes.forEach { backplane ->
            logger.info(" - Starting backplane: {}", backplane)
            executor.execute {
                backplane.listen { handleReceivedNotification(it) }
            }
        }
    }

    private fun handleReceivedNotification(notification: Notification) {
        logger.trace("Received notification: {}", notification)
        if (notification.instanceId != null && notification.instanceId == applicationProperties.instanceId) {
            logger.debug("Ignoring own notification: {}", notification)
        } else {
            logger.debug("Propagating received backplane notification: {}", notification)
            clientStreams.push(FeatureChangedEvent(notification.featureId, notification.environmentIds))
        }
    }

    override fun destroy() {
        executor.shutdown()
    }

    @TransactionalEventListener(FeatureChangedEvent::class, phase = TransactionPhase.AFTER_COMMIT)
    @Async
    open fun receiveClientChangedEvent(event: FeatureChangedEvent) {
        logger.debug("Pushing FeatureChangeEvent through backplanes")
        backplanes.forEach { backplane ->
            backplane.sendNotification(Notification(
                featureId = event.featureId,
                environmentIds = event.environmentIds,
                instanceId = applicationProperties.instanceId,
            ))
        }
    }
}
