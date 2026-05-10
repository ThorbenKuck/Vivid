package com.vivid.backend.backplane.postgres

import com.vivid.backend.backplane.NotificationBackplane
import com.vivid.backend.backplane.dto.Notification
import javax.sql.DataSource
import org.postgresql.PGConnection
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import kotlin.jvm.java

private val logger = LoggerFactory.getLogger(PostgresBackplane::class.java)

class PostgresBackplane(
    private val dataSource: DataSource,
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
): NotificationBackplane {

    var active = true

    override fun listen(callback: (Notification) -> Unit) {
        while (active) {
            try {
                dataSource.connection.use { connection ->
                    // Wir brauchen die native Postgres-Verbindung für den Zugriff auf PGConnection
                    val pgConnection = connection.unwrap(PGConnection::class.java)
                    connection.createStatement().use { stmt ->
                        stmt.execute("LISTEN feature_change_channel")
                    }
                    while (!Thread.currentThread().isInterrupted) {
                        // Abfrage, ob neue Benachrichtigungen vorliegen (Polling am Treiber, nicht an der DB)
                        val notifications = pgConnection.getNotifications(5000) // 5 Sek Timeout

                        if (notifications != null) {
                            for (notification in notifications) {
                                val notification: String? = notification.parameter
                                if (notification == null) {
                                    logger.warn("Postgres returned null notification.")
                                } else {
                                    logger.debug("Postgres returned notification: $notification")
                                    callback(objectMapper.readValue(notification, Notification::class.java))
                                }
                            }
                        }
                    }
                }
            } catch (_: InterruptedException) {
                logger.info("PostgresBackplane was interrupted. Shutting down.")
                active = false
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                if (Thread.currentThread().isInterrupted.not()) {
                    logger.warn("Error in PostgresBackplane: ${e.message}. Attempting reconnect in 5 seconds.", e)
                    Thread.sleep(5000)
                } else {
                    logger.info("PostgresBackplane was interrupted. Shutting down.")
                    active = false
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    override fun sendNotification(notification: Notification) {
        jdbcTemplate.execute("NOTIFY feature_change_channel, '" + objectMapper.writeValueAsString(notification) + "'");
    }

    override fun toString(): String {
        return "PostgresBackplane()"
    }
}